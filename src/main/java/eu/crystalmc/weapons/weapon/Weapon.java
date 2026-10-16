package eu.crystalmc.weapons.weapon;

import com.google.gson.annotations.SerializedName;
import eu.crystalmc.weapons.config.WeaponConfig;

public class Weapon extends WeaponConfig {

    @SerializedName("ammunition")
    private int ammunition;

    @SerializedName("id")
    private String id;

    @SerializedName("runtime")
    private String runtime;

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

    public String getRuntime() {return runtime; }

    public void setRuntime(String runtime) { this.runtime = runtime; }
}