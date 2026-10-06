package eu.crystalmc.weapons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.OverlappingFileLockException;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZIPUtils {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private ZIPUtils() {
    }

    public static void zipFolder(File folder, File zipFile) throws IOException {
        zipFolder(folder, new FileOutputStream(zipFile));
    }

    public static void zipFolder(File folder, OutputStream outputStream) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            processFolder(folder, zipOutputStream, folder.getPath().length() + 1);
        }
    }

    private static void processFolder(
            File folder,
            ZipOutputStream zipOutputStream,
            int prefixLength
    ) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                String entryName = file.getPath()
                        .substring(prefixLength)
                        .replace(File.separatorChar, '/');

                ZipEntry zipEntry = new ZipEntry(entryName);

                if (shouldStore(file)) {
                    zipEntry.setMethod(ZipEntry.STORED);
                    zipEntry.setSize(file.length());
                    zipEntry.setCrc(calculateCrc(file));
                } else {
                    zipEntry.setMethod(ZipEntry.DEFLATED);
                }

                zipOutputStream.putNextEntry(zipEntry);

                if (!isFileLocked(file)) {
                    try (FileInputStream inputStream = new FileInputStream(file)) {
                        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            zipOutputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }

                zipOutputStream.closeEntry();
            } else if (file.isDirectory()) {
                processFolder(file, zipOutputStream, prefixLength);
            }
        }
    }

    private static boolean shouldStore(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return false;
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase();

        Set<String> storedExtensions = Set.of(
                "zip",
                "jar",
                "gz",
                "png",
                "jpg",
                "jpeg",
                "gif",
                "mp4"
        );

        return storedExtensions.contains(extension);
    }

    private static long calculateCrc(File file) throws IOException {
        CRC32 crc = new CRC32();

        try (FileInputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            while (true) {
                int read = input.read(buffer);
                if (read == -1) {
                    break;
                }

                crc.update(buffer, 0, read);
            }
        }

        return crc.getValue();
    }

    private static boolean isFileLocked(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             var channel = raf.getChannel()) {
            var lock = channel.tryLock();
            if (lock != null) {
                lock.release();
                return false;
            } else {
                return true;
            }
        } catch (OverlappingFileLockException | IOException e) {
            return true;
        }
    }
}