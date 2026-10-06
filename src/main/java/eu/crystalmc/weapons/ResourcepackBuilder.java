package eu.crystalmc.weapons;

import eu.crystalmc.weapons.config.Config;
import eu.crystalmc.weapons.config.WeaponConfig;
import eu.crystalmc.weapons.utils.ZIPUtils;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class ResourcepackBuilder {
    CrystalWeapons plugin;

    ResourcepackBuilder(CrystalWeapons plugin) {
        this.plugin = plugin;
    }


    public void init() {
        Config.Data config = plugin.getConfiguration();
        Path path = plugin.getDataPath();

        File outputFolder = path.resolve("output").toFile();





        if (!makeDir(outputFolder)) return;

        // clear output folder
        try {
            FileUtils.cleanDirectory(outputFolder);
        } catch(Exception e) {
            plugin.sendError("Error when cleaning output folder.");
        }

        File weaponsFolder = path.resolve("weapons").toFile();

        File texture;
        String name;
        WeaponConfig weaponConfig;

        for (File file : Objects.requireNonNull(weaponsFolder.listFiles())) {
            name = file.getName().toLowerCase();
            if (name.endsWith(".json")) {
                texture = weaponsFolder.toPath().resolve(name.replace(".json", ".png")).toFile();
                if (texture.exists()) {

                }
            }
        }
    }

    private void createWeaponFiles(config) {

    }

    private void pack(File folder, File output) throws IOException {
        ZIPUtils.zipFolder(folder, output);
        for (String path : plugin.getConfiguration().resourcepack.copyTo) {
            Files.copy(output.toPath(), Path.of(path), StandardCopyOption.REPLACE_EXISTING);
        }
    }


    // return false if file does not exist. returns true if it was created or already exists.
    private boolean makeDir(File file) {
        boolean result = true;
        if (!file.exists()) {
            result = file.mkdirs();
        }
        return result;
    }
}
