package eu.crystalmc.weapons;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import eu.crystalmc.weapons.utils.WeaponUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class SynchronizeWeaponWithConfiguration implements Listener {
    CrystalWeapons plugin;

    SynchronizeWeaponWithConfiguration(CrystalWeapons plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onEquip(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();


        ItemStack item = player.getInventory().getItem(event.getNewSlot());

        Weapon weapon = WeaponUtils.getWeapon(item);
        if (weapon == null) return;



        if (!weapon.isSynchronizedWithConfiguration() && weapon.isSynchronizeWeaponData()) {
            ItemStack newItem = WeaponsRegistry.getWeapon(weapon.getId());

            assert item != null;
            NBT.modify(item, itemNBT -> {
                NBT.get(newItem, newItemNBT -> {
                    ReadWriteNBT compound = WeaponUtils.getNBT(itemNBT);

                    compound.mergeCompound(WeaponUtils.getItemNBT(newItemNBT));
                    compound.setInteger("ammunition", weapon.getAmmunition());
                });
            });

            Weapon newWeapon = WeaponUtils.getWeapon(item);

            WeaponLogic.updateLore(item, newWeapon);

            plugin.sendInfo("Synchronized weapon of " + player.getName() + " with configuration");
            player.getInventory().setItem(event.getNewSlot(), item);

        }
    }
}

