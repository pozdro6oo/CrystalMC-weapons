package eu.crystalmc.weapons.config.resourcepack;

import com.google.gson.annotations.SerializedName;

public class Model {

    @SerializedName("textures")
    public Textures textures = new Textures();

    @SerializedName("parent")
    public String parent = "minecraft:generated";



    // for gson to work
    public Model() {
    }

    public static class Textures {
        Textures() {}

        @SerializedName("layer0")
        public String layer0 = "minecraft:item/paper";



    }

}