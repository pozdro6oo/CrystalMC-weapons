package eu.crystalmc.weapons;

import com.google.gson.annotations.SerializedName;
import eu.crystalmc.weapons.config.WeaponConfig;

public class Weapon extends WeaponConfig {

    private int bullets;

    private String id;

    @SerializedName("dedicated_model")
    private String dedicatedModel;



    public int getBullets() {
        return bullets;
    }

    public void setBullets(int bullets) {
        this.bullets = bullets;
    }

    public String getDedicatedModel() {
        return dedicatedModel;
    }

    public String getId() {
        return id;
    }
}