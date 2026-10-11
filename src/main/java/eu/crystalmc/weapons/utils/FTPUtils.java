package eu.crystalmc.weapons.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static it.unimi.dsi.fastutil.io.TextIO.BUFFER_SIZE;

public final class FTPUtils {
    private FTPUtils() {

    }

    public static void uploadFile(String host, String user, String password, String filePath, String uploadPath) throws IOException {
        String ftpUrl = String.format("ftp://%s:%s@%s/%s;type=i", user, password, host, uploadPath);
        System.out.println("Upload URL: " + ftpUrl);

        URL url = new URL(ftpUrl);
        URLConnection conn = url.openConnection();
        OutputStream outputStream = conn.getOutputStream();
        FileInputStream inputStream = new FileInputStream(filePath);

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        System.out.println("File uploaded");

    }
}
