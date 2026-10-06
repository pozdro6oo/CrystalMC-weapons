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
        if (this.isSetup()) {
            setupDefaultConfiguration();
        }

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

    public void sendError(String message, Exception exception) {
        log(message + " Exception: " + exception.getClass().getName(), NamedTextColor.RED);
    }

    private void log(String message, NamedTextColor color) {
        Bukkit.getConsoleSender().sendMessage(Component.text(message).color(color));
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
        this.saveResource("assets/textures/bullet_hit.png", false);
        this.saveResource("assets/textures/fire_textures/weapon_gun_fire.png", false);
        this.saveResource("assets/textures/fire_textures/taser_fire.png", false);

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

        this.saveResource("tutorial.txt", false);
    }

}
