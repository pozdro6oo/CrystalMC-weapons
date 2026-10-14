package eu.crystalmc.weapons.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;


public class NBTUtils {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static <T> ReadWriteNBT nbtFromObject(T object) {
        return NBT.parseNBT(getRaw(object));
    }


    public static <T> T nbtToObject(ReadableNBT nbt, Class<T> objectClass) {
        try {
            return fromString(nbtToJson(nbt), objectClass);
        } catch (Exception e) {
            return null;
        }
    }


    private static <T> T fromString(String raw, Class<T> objectClass) {
        return gson.fromJson(raw, objectClass);
    }

    private static <T> String getRaw(T object) {
        return gson.toJson(object);
    }

    private static String nbtToJson(ReadableNBT nbt) {
        return nbt.toString()
                .replaceAll(":\\s*1b(?=\\s*[,}])", ":true")
                .replaceAll(":\\s*0b(?=\\s*[,}])", ":false");
    }
}