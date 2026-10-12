package eu.crystalmc.weapons.utils;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;
import java.util.logging.Level;

public class NBTUtils {

    public static ReadWriteNBT toCompound(Object anyClass) {
        ReadWriteNBT compound = NBT.createNBTObject();

        for (Class<?> c = anyClass.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                SerializedName annotation = field.getAnnotation(SerializedName.class);
                String key = annotation != null ? annotation.value() : field.getName();

                Object value;
                try {
                    field.setAccessible(true);
                    value = field.get(anyClass);
                } catch (IllegalAccessException | RuntimeException e) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not read '" + key + "': " + e.getMessage());
                    continue;
                }

                switch (value) {
                    case String s -> compound.setString(key, s);
                    case Double d -> compound.setDouble(key, d);
                    case Integer i -> compound.setInteger(key, i);
                    case Float f -> compound.setFloat(key, f);
                    case Byte b -> compound.setByte(key, b);
                    case Boolean b -> compound.setBoolean(key, b);
                    case ItemStack item -> compound.setItemStack(key, item);
                    case UUID uuid -> compound.setUUID(key, uuid);
                    case Short sh -> compound.setShort(key, sh);
                    case Enum<?> e -> compound.setEnum(key, e);
                    case null, default -> Bukkit.getLogger().log(Level.WARNING, "'" + key + "' = '" + value + "' is not a supported NBT type");
                }
            }
        }

        return compound;
    }
}