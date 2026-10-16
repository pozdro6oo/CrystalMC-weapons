package eu.crystalmc.weapons;

import com.google.gson.annotations.SerializedName;
import eu.crystalmc.weapons.config.WeaponConfig;

public class Weapon extends WeaponConfig {

    private int ammunition;

    private String id;

    @SerializedName("dedicated_model")
    private String dedicatedModel;



    public int getAmmunition() {
        return ammunition;
    }

    public void setAmmunition(int ammunition) {
        this.ammunition = ammunition;
    }

    public String getDedicatedModel() {
        return dedicatedModel;
    }

    public String getId() {
        return id;
    }
}