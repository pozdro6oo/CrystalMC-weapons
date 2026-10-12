package eu.crystalmc.weapons;

import de.tr7zw.nbtapi.NBT;
import eu.crystalmc.weapons.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;

public final class BulletHitDecals {

    private static final Deque<ItemDisplay> decals = new ArrayDeque<>();

    private BulletHitDecals() {
    }

    public static void start(ItemDisplay displayHit, Block hitBlock, JavaPlugin plugin, Config.Data data) {
        int limit = data.weapon.visual.bulletHitDecal.limit;

        NBT.modify(displayHit, nbt -> {
            nbt.setBoolean("is_bullet_hit_decal", true);
        });

        if (limit <= 0) {
            displayHit.remove();
            return;
        }

        while (decals.size() >= limit) {
            ItemDisplay oldest = decals.pollFirst();

            if (oldest != null && oldest.isValid()) {
                oldest.remove();
            }
        }

        decals.addLast(displayHit);

        final int[] timer = {0};
        int disappearAfter = data.weapon.visual.bulletHitDecal.disappearAfter*20;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!displayHit.isValid()) {
                    decals.remove(displayHit);
                    cancel();
                    return;
                }

                if (timer[0] >= disappearAfter || !hitBlock.getType().isSolid()) {
                    displayHit.remove();
                    decals.remove(displayHit);
                    cancel();
                    return;
                }

                timer[0]++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }


    public static void startGarbageCollector(JavaPlugin plugin) {
        int interval = 240*20;
        new BukkitRunnable() {@Override public void run() {

            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntitiesByClass(ItemDisplay.class)) {
                    NBT.get(entity, nbt -> {
                        boolean isBulletHitDecal = nbt.getBoolean("is_bullet_hit_decal");
                        if (isBulletHitDecal && !decals.contains(entity)) {
                            plugin.getLogger().log(Level.WARNING, "Removed old decal " + entity.getEntityId());
                            entity.remove();
                        }
                    });
                }
            }


            }}.runTaskTimer(plugin, 0, interval);
    }
}