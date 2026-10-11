package eu.crystalmc.weapons.config.resourcepack;


import com.google.gson.annotations.SerializedName;
import java.util.List;


public class Sound {

    @SerializedName("sounds")
    private List<String> sounds;

    public Sound() {
    }

    public List<String> getSounds() {
        return sounds;
    }
}