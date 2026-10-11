package eu.crystalmc.weapons;

import eu.crystalmc.weapons.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
//import eu.crystalmc.weapons.config.WeaponConfig
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class CrystalWeapons extends JavaPlugin {

    @NotNull
    Path configPath = this.getDataPath().resolve("config.yml");

    Config config;

    @Override
    public void onEnable() {

        // save default resources on first start of the plugin
        setupDefaultConfiguration();

        // load or create config.yml
        config = new Config(this.configPath);


        //  register event listener
        //  this one does not need any priority
        Bukkit.getPluginManager().registerEvents(new WeaponLogic(this), this);


        //  build and pack resourcepack
        ResourcepackBuilder builder = new ResourcepackBuilder(this);
        builder.init();
    }

    public void sendWarning(String message) {
        log(message, NamedTextColor.YELLOW);
    }

    public void sendError(String message) {
        log(message, NamedTextColor.RED);
    }

    public void sendInfo(String message) {
        log(message, NamedTextColor.WHITE);
    }

    private void log(String message, NamedTextColor color) {
        Bukkit.getConsoleSender().sendMessage(getLogPrefix().append(Component.text(message).color(color)));
    }

    private Component getLogPrefix() {
        return
                Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("Crystal").color(NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("Weapons").color(NamedTextColor.DARK_PURPLE))
                .append(Component.text("] ").color(NamedTextColor.DARK_GRAY));
    }

    public @NotNull Config.Data getConfiguration() {
        return config.data;
    }

    public void reloadConfiguration() {
        config.reload(configPath);
    }

    public void saveConfiguration() {
        config.save(configPath);
    }

    private Boolean isSetup() {
        return !configPath.toFile().exists();
    }

    private void setupDefaultConfiguration() {
        if (this.isSetup()) {
            this.saveResource("assets/textures/bullet_hit.png", false);
            this.saveResource("assets/textures/weapon_fire/default.png", false);
            this.saveResource("assets/textures/weapon_fire/taser.png", false);

            this.saveResource("weapons/ak47.png", false);
            this.saveResource("weapons/mag7.png", false);
            this.saveResource("weapons/glock18.png", false);

            this.saveResource("weapons/ak47.json", false);
            this.saveResource("weapons/mag7.json", false);
            this.saveResource("weapons/glock18.json", false);

            this.saveResource("assets/sounds/ak47.reload.ogg", false);
            this.saveResource("assets/sounds/ak47.shoot.ogg", false);

            this.saveResource("assets/sounds/mag7.reload.ogg", false);
            this.saveResource("assets/sounds/mag7.shoot.ogg", false);

            this.saveResource("assets/sounds/glock18.reload.ogg", false);
            this.saveResource("assets/sounds/glock18.shoot.ogg", false);

            this.saveResource("assets/models/weapon/default.json", false);
            this.saveResource("assets/models/weapon_fire/default.json", false);
            this.saveResource("assets/models/weapon_fire/taser.json", false);

            this.saveResource("tutorial.txt", false);
        }

        String bulletHitModel = "assets/models/bullet_hit.json";
        if (!this.getDataPath().resolve(bulletHitModel).toFile().exists()) {
            this.saveResource(bulletHitModel, false);
        }
    }

}
