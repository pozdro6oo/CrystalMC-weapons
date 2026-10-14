package eu.crystalmc.weapons.resourcepack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.WeaponsRegistry;
import eu.crystalmc.weapons.config.Config;
import eu.crystalmc.weapons.config.JsonConfig;
import eu.crystalmc.weapons.config.WeaponConfig;
import eu.crystalmc.weapons.config.resourcepack.Item;
import eu.crystalmc.weapons.config.resourcepack.Model;
import eu.crystalmc.weapons.utils.FTPUtils;
import eu.crystalmc.weapons.utils.HTTPServer;
import eu.crystalmc.weapons.utils.NBTUtils;
import eu.crystalmc.weapons.utils.ZIPUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ResourcepackBuilder {
    CrystalWeapons plugin;

    public ResourcepackBuilder(CrystalWeapons plugin) {
        this.plugin = plugin;
    }


    public void init() {
        File outputFolder = getResourcepackFolder().toFile();
        Path crystalFolder = getCrystalmcFolder();



        try {
            FileUtils.forceMkdir(outputFolder);

            FileUtils.cleanDirectory(outputFolder);

            Path assets = outputFolder.toPath().resolve("assets");


            Files.createDirectories(assets);
            Files.createDirectories(crystalFolder.resolve("items"));
            Files.createDirectories(crystalFolder.resolve("sounds"));
            Files.createDirectories(crystalFolder.resolve("textures").resolve("item").resolve("weapon"));
            Files.createDirectories(crystalFolder.resolve("models"));
            Files.createDirectories(crystalFolder.resolve("textures"));

        } catch (IOException e) {
            plugin.sendError("Error preparing resourcepack folders");
            return;
        }


        plugin.saveResource("resourcepack/pack.png", true);
        plugin.saveResource("resourcepack/pack.mcmeta", true);

        createWeapons();

        createSounds();

        copySubAssets("textures", "item");
        copySubAssets("models", null);

        createBulletHitItem();


        File resourcepackOutput = outputFolder.toPath().getParent().resolve(outputFolder.getName() + ".zip").toFile();

        pack(outputFolder, resourcepackOutput);

        host(resourcepackOutput);
    }


    private void createWeapons() {
        File weaponsFolder = plugin.getDataPath().resolve("weapons").toFile();
        int weaponCount = 0;



        File[] files = weaponsFolder.listFiles((_, name) -> name.endsWith(".json"));

        if (files == null) {
            plugin.sendWarning("No weapons found.");
            return;
        }

        File texture;
        String name;
        JsonConfig<WeaponConfig> weaponConfig;

        for (File file : files) {
            name = file.getName().toLowerCase();
            if (name.endsWith(".json")) {
                texture = weaponsFolder.toPath().resolve(name.replace(".json", ".png")).toFile();
                if (texture.exists()) {
                    weaponConfig = new JsonConfig<>(file, WeaponConfig.class);
                    name = name.replaceFirst("[.][^.]+$", "");
                    boolean generated = createWeaponFiles(name, weaponConfig, texture);
                    if (generated) {
                        weaponCount++;
                    } else {
                        plugin.sendWarning("Failed to register weapon '" + name + "'.");
                    }
                }
            }
        }
        plugin.sendInfo("Registered weapons: " + weaponCount);

    }

    private boolean createWeaponFiles(String name, JsonConfig<WeaponConfig> weaponConfig, File texture) {
        weaponConfig.load();
        WeaponConfig weapon = weaponConfig.config;

        try {
            Files.copy(texture.toPath(), getCrystalmcFolder().resolve("textures").resolve("item").resolve("weapon").resolve(texture.getName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.sendError("Failed to create a weapon texture.");
            return false;
        }

        ItemStack item = createWeaponItemStack(name, weapon);

        if (item == null) {
            return false;
        }

        weaponConfig.save();

        createWeaponModel(name, weapon);
        createWeaponItem(name, weapon);

        WeaponsRegistry.registerWeapon(name, item);

        return true;
    }


    private ItemStack createWeaponItemStack(String name, WeaponConfig weapon) {

        // check if item is a real material
        boolean isMaterial = EnumUtils.isValidEnum(Material.class, getFormattedMaterial(weapon.getItem()));
        if (!isMaterial) {
            plugin.sendError("Weapon '" + name + "' has invalid material.");
            return null;
        }


        // create item
        Material material = Material.valueOf(weapon.getItem());
        ItemStack item = new ItemStack(material);


        ReadWriteNBT compound = NBTUtils.nbtFromObject(weapon);
        compound.setString("dedicated_model", "crystalmc:weapon/" + name);
        compound.setInteger("bullets", weapon.getMaxBullets());
        compound.setString("id", name);

        NBT.modify(item, nbt -> {
                nbt.getOrCreateCompound(plugin.COMPOUND).mergeCompound(compound);
            });

        ItemMeta meta = item.getItemMeta();


        Component itemName = Component.text(weapon.getDisplayName()).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
        meta.itemName(itemName);
        meta.customName(itemName);
        meta.setItemModel(new NamespacedKey("crystalmc", "weapon/" + name));

        Double fireRate = Math.floor((20.0 / weapon.getShotDelay()) * 1000) / 1000;
        Double accuracy = 1 - weapon.getInaccuracy();

        meta.lore(List.of(
            getLoreLine("Ammunition: " + weapon.getMaxBullets() + "/" + weapon.getMaxBullets()),
            Component.empty(),
            getLoreLine("Statistics:", NamedTextColor.GRAY),
            getLoreLine(weapon.getDamage() + " Damage"),
            getLoreLine(fireRate + " Fire Rate"),
            getLoreLine(accuracy + " Accuracy"),
            getLoreLine(weapon.getRecoil() + " Recoil")
            )
        );

        Consumable.Builder builder = Consumable.consumable()
            .hasConsumeParticles(false)
            .animation(ItemUseAnimation.TOOT_HORN)
            .consumeSeconds(20);

        meta.setMaxStackSize(1);
        item.setItemMeta(meta);
        item.setData(DataComponentTypes.CONSUMABLE, builder.build());

        return item;
    }

    private Component getLoreLine(String string, NamedTextColor color) {
        return Component.text(string).decoration(TextDecoration.ITALIC, false).color(color);
    }
    private Component getLoreLine(String string) {
        return getLoreLine(string, NamedTextColor.BLUE);
    }

    private void createWeaponModel(String name, WeaponConfig weaponConfig) {
        File modelFile = getCrystalmcFolder().resolve("models").resolve("weapon").resolve(name + ".json").toFile();

        JsonConfig<Model> model = new JsonConfig<>(modelFile, Model.class);

        model.load();
        model.config.textures.layer0 = "crystalmc:item/weapon/" + name;
        model.config.parent = "crystalmc:weapon/" + weaponConfig.getModel();

        model.save();
    }

    private void createWeaponItem(String name, WeaponConfig weaponConfig) {
        File modelFireFile = getCrystalmcFolder().resolve("items").resolve("weapon").resolve(name + "_fire.json").toFile();

        JsonConfig<Item> modelFire = new JsonConfig<>(modelFireFile, Item.class);

        modelFire.load();
        modelFire.config.model.type = "minecraft:composite";
        modelFire.config.model.model = "";
        modelFire.config.model.models = List.of(
                new Item.CustomModel("minecraft:model", "crystalmc:weapon/" + name),
                new Item.CustomModel("minecraft:model", "crystalmc:weapon_fire/" + weaponConfig.getGunFireAnimationModel())
        );
        modelFire.save();


        File modelFile = getCrystalmcFolder().resolve("items").resolve("weapon").resolve(name + ".json").toFile();

        JsonConfig<Item> model = new JsonConfig<>(modelFile, Item.class);

        model.load();
        model.config.model.type = "minecraft:model";
        model.config.model.model = "";
        model.config.model.model = "crystalmc:weapon/" + name;
        model.save();


    }

    private void createBulletHitItem() {
        File modelFile = getCrystalmcFolder().resolve("items").resolve("bullet_hit.json").toFile();

        JsonConfig<Item> model = new JsonConfig<>(modelFile, Item.class);

        model.load();
        model.config.model.type = "minecraft:model";
        model.config.model.model = "";
        model.config.model.model = "crystalmc:bullet_hit";
        model.save();
    }







    private void createSounds() {
        Path folder = plugin.getDataPath().resolve("assets").resolve("sounds");

        Path outputFolder = getCrystalmcFolder().resolve("sounds");

        File[] files = folder.toFile().listFiles((_, name) -> name.endsWith(".ogg"));

        if (files == null) {
            plugin.sendWarning("No sounds found.");
            return;
        }

        Path output;
        for (File file : files) {
            try {
                output = outputFolder.resolve(file.getName()).toAbsolutePath();
                Files.copy(file.toPath().toAbsolutePath(), output);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        createSoundsFile(files);

    }
    private void createSoundsFile(File[] sounds) {
        Path output = getCrystalmcFolder().resolve("sounds.json").toAbsolutePath();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject json = new JsonObject();

        JsonObject sound;
        List<String> soundsList;

        String soundName;

        for (File file : sounds) {
            soundName = file.getName().replaceFirst("[.][^.]+$", "");
            sound = new JsonObject();
            soundsList = List.of("crystalmc:" + soundName);



            sound.add("sounds", gson.toJsonTree(soundsList));


            json.add("weapon."+ soundName, sound);
        }



        try {
            Files.writeString(output, json.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.sendError("Error while writing '" + output + "'.");
        }
    }

    private void copySubAssets(String subFolder, String add) {
        Path crystalFolder = getCrystalmcFolder();

        File folder = plugin.getDataPath().resolve("assets").resolve(subFolder).toFile();


        Path outputFolder = crystalFolder.resolve(subFolder);

        if (add != null) {
            outputFolder = outputFolder.resolve(add);
        }

        try {
            Files.createDirectories(outputFolder);

            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (file.isDirectory()) {
                    copyEverything(file.toPath(), outputFolder.resolve(file.getName()));
                } else {
                    Path output = outputFolder.resolve(file.getName());
                    Files.copy(file.toPath(), output, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            plugin.sendError("Error while copying files. ('" + subFolder + "')");
        }
    }




    private void host(File resourcepack) {
        Config.Data data = plugin.getConfiguration();
        try {
            switch (data.resourcepack.mode) {
                case "upload" -> {
                    hostUpload(resourcepack, data.resourcepack.modeUpload);
                }
                case "provided" -> {
                    return;
                }
                // auto
                default -> {
                    hostAuto(resourcepack, data.resourcepack.modeAuto);
                    String address = "http://" + data.resourcepack.modeAuto.address + ":" + data.resourcepack.modeAuto.port + "/" + resourcepack.getName();
                    addResourcepack(address, resourcepack.toPath());
                }
            }
        } catch (IOException e) {
            plugin.sendError("Error while trying to host/upload resourcepack.");
        } catch (URISyntaxException | NoSuchAlgorithmException e) {
            plugin.sendError("Error while trying add resourcepack.");
        }
    }

    private void hostAuto(File resourcepack, Config.Data.Resourcepack.ModeAuto config) throws IOException {
        new BukkitRunnable() {
            @Override
            public void run() {
                HTTPServer server = new HTTPServer();
                server.setPath(resourcepack.toPath().toAbsolutePath());
                try {
                    server.host(config.port);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskAsynchronously(plugin);

    }

    private void hostUpload(File resourcepack, Config.Data.Resourcepack.ModeUpload config) throws IOException {
        FTPUtils.uploadFile(config.host, config.user, config.password, resourcepack.getAbsolutePath(), config.uploadFile);
    }






    private void pack(File folder, File output) {
        try {
            ZIPUtils.zipFolder(folder, output);
        } catch (IOException e) {
            plugin.sendError("Error while packing resourcepack.");
            return;
        }

        try {
            for (String path : plugin.getConfiguration().resourcepack.copyResourcepackOutputTo) {
                Files.copy(output.toPath(), Path.of(path), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            plugin.sendError("Error while coping resourcepack outputs.");
        }
    }

    private Path getResourcepackFolder() {
        return plugin.getDataPath().resolve("resourcepack");
    }

    private Path getCrystalmcFolder() {
        return getResourcepackFolder().resolve("assets").resolve("crystalmc");
    }

    // return false if file does not exist. returns true if it was created or already exists.
    private boolean makeDir(Path p) {
        Path path = p.toAbsolutePath();
        try {
//            Files.createDirectories(path);
            return true;
        } catch(Exception e) {
            plugin.sendError("Error while creating '" + path + "'.");
            return false;
        }
    }

    private void copyEverything(Path source, Path target) throws IOException {
        Files.createDirectories(target);

        try (var files = Files.list(source)) {
            for (Path file : files.toList()) {
                Path output = target.resolve(file.getFileName());

                if (Files.isDirectory(file)) {
                    copyEverything(file, output);
                } else {
                    Files.copy(file, output, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private String getFormattedMaterial(String material) {
        return material.replaceAll(" ", "_").toUpperCase();
    }









    private void addResourcepack(String address, Path path) throws URISyntaxException, NoSuchAlgorithmException, IOException {
        UUID uuid = UUID.randomUUID();

        ResourcePackInfo packInfo = ResourcePackInfo.resourcePackInfo()
                .id(uuid)
                .hash(getSHA1(path))
                .uri(new URI(address))
                .build();


        ResourcePackRequest packRequest = ResourcePackRequest.resourcePackRequest()
                .packs(packInfo)
                .required(true)
                .prompt(Component.text("Please download resourcepack"))
                .build();


        Bukkit.getPluginManager().registerEvents(new Send(packRequest), plugin);
    }


    public static String getSHA1(Path path) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }
}
