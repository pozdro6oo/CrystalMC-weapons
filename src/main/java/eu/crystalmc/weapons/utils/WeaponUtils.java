package eu.crystalmc.weapons.utils;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableItemNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.Weapon;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class WeaponUtils {
    public static Weapon getWeapon(ItemStack item) {
        if (!isWeapon(item)) return null;

        Weapon[] weaponHolder = new Weapon[1];

        NBT.get(item, nbt -> {
            ReadableNBT compound = WeaponUtils.getItemNBT(nbt);
            if (compound != null) {
                weaponHolder[0] = NBTUtils.nbtToObject(compound, Weapon.class);
            }
        });

        return weaponHolder[0];
    }

    public static boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        ReadableNBT[] compound = new ReadableNBT[1];

         NBT.get(item, nbt -> {
            compound[0] = getItemNBT(nbt);
        });


        return compound[0] != null;
    }


    public static String getFireAnimationModel(Weapon weapon) {
        return "crystalmc:weapon/" + weapon.getId() + "_fire";
    }

    public static String getModel(Weapon weapon) {
        return "crystalmc:weapon/" + weapon.getId();
    }

    public static NamespacedKey createCooldownGroup() {
        return NamespacedKey.fromString("crystalmc:weapon/"+ UUID.randomUUID().toString().replaceAll("-", ""));
    }

    public static ReadWriteNBT getNBT(ReadWriteNBT nbt) {
        return nbt.getCompound(CrystalWeapons.COMPOUND);
    }
    public static ReadableNBT getItemNBT(ReadableItemNBT nbt) {
        return nbt.getCompound(CrystalWeapons.COMPOUND);
    }
}
