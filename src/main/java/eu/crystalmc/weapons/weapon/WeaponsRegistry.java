package eu.crystalmc.weapons.weapon;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class WeaponsRegistry {
    private static final HashMap<String, ItemStack> weapons = new HashMap<>();

    private WeaponsRegistry() {

    }

    public static Map<String, ItemStack> getWeapons() {
        return weapons;
    }

    public static void registerWeapon(String id, ItemStack item) {
        WeaponsRegistry.weapons.put(id, item);
    }

    public static ItemStack getWeapon(String id) {
        return weapons.get(id);
    }
}