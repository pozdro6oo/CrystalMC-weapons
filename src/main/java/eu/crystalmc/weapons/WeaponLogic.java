package eu.crystalmc.weapons;

import de.tr7zw.nbtapi.NBT;
import eu.crystalmc.weapons.utils.NBTUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.bukkit.util.Transformation;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class WeaponLogic implements Listener {

    private final CrystalWeapons plugin;

    public WeaponLogic(CrystalWeapons plugin) {
        this.plugin = plugin;
    }


    public ItemStack createBulletHitTexture() {
        ItemStack item = new ItemStack(Material.BLACK_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setItemModel(NamespacedKey.fromString("crystalmc:bullet_hit"));
            item.setItemMeta(meta);
        }
        return item;
    }

    // Display bullets when player equips a weapon
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEquip(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null) return;

        Weapon weapon = getWeaponData(item);
        if (weapon == null) return;

        displayBullets(player, weapon.getBullets(), weapon.getMaxBullets());
    }

    // Prevent player from dropping a fired weapon
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDrop(PlayerDropItemEvent event) {
        ItemMeta meta = event.getItemDrop().getItemStack().getItemMeta();
        if (meta != null && meta.getItemModel() != null) {
            if (meta.getItemModel().toString().contains("_fire")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRightClick(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getEquipment().getItem(EquipmentSlot.HAND);

        Weapon weapon = getWeaponData(item);
        if (weapon == null) return;

        useWeapon(event.getPlayer(), item, weapon);
    }

    public void useWeapon(Player player, ItemStack item, Weapon weapon) {
        new BukkitRunnable() {
            private int timer = 0;

            @Override
            public void run() {
                if (!player.isHandRaised() || !player.isOnline() || !item.equals(player.getActiveItem())) {
                    this.cancel();
                    return;
                }

                if (weapon.getBullets() == 0) {
                    reload(player, weapon, item);
                    this.cancel();
                    return;
                }

                if (timer == weapon.getShotDelay()) {
                    timer = 0;
                }

                if (timer == 0) {
                    if (!weapon.isInfiniteAmmo()) {
                        weapon.setBullets(weapon.getBullets() - 1);
                    }
                    displayBullets(player, weapon.getBullets(), weapon.getMaxBullets());
                    shoot(player, weapon, item);

                    if (!weapon.isAutomatic()) {
                        this.cancel();
                        return;
                    }
                }

                timer++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void showHotbar(Player player, String message) {
        player.sendActionBar(Component.text(message));
    }

    public Sound playSound(Player player, String soundKey, float pitch) {
        Sound sound = Sound.sound(Key.key(soundKey), Sound.Source.PLAYER, 1.0f, pitch);
        player.playSound(sound, player.getLocation());
        return sound;
    }

    public Component getBlueText(String string) {
        return Component.text(string).color(NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false);
    }

    public void reload(Player player, Weapon weapon, ItemStack item) {
        player.setCooldown(item, weapon.getReloadTime());

        Sound sound = playSound(player, "crystalmc:weapon." + weapon.getReloadSound(), 1.0F);

        new BukkitRunnable() {
            private int timer = 0;

            @Override
            public void run() {
                showHotbar(player, "Reloading");

                player.getEquipment();
                if (!item.equals(player.getEquipment().getItem(player.getActiveItemHand()))) {
                    showHotbar(player, "Reload cancelled");
                    player.stopSound(sound);
                    player.setCooldown(item, 0);
                    this.cancel();
                    return;
                }

                if (timer == weapon.getReloadTime()) {
                    showHotbar(player, "Reloaded");
                    NBT.modify(item, nbt -> {
                        var compound = nbt.getCompound(plugin.COMPOUND);
                        if (compound != null) {
                            compound.setInteger("bullets", weapon.getMaxBullets());
                        }
                    });

                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        List<Component> lore = meta.lore();
                        if (lore != null && !lore.isEmpty()) {
                            lore.set(0, getBlueText("Ammunition: " + weapon.getMaxBullets() + "/" + weapon.getMaxBullets()));
                            meta.lore(lore);
                            item.setItemMeta(meta);
                        }
                    }

                    this.cancel();
                    return;
                }
                timer++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void shoot(Player player, Weapon weapon, ItemStack item) {
        String model = weapon.getDedicatedModel();
        String modelFire = model + "_fire";

        if (plugin.getConfiguration().weapon.visual.enableFireAnimation) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setItemModel(NamespacedKey.fromString(modelFire));

                List<Component> lore = meta.lore();
                if (lore != null && !lore.isEmpty()) {
                    lore.set(0, getBlueText("Ammunition: " + weapon.getBullets() + "/" + weapon.getMaxBullets()));
                    meta.lore(lore);
                }
                item.setItemMeta(meta);
            }
        }

        NBT.modify(item, nbt -> {
            var compound = nbt.getCompound(plugin.COMPOUND);
            if (compound != null) {
                compound.setInteger("bullets", weapon.getBullets());
            }
        });

        if (plugin.getConfiguration().weapon.visual.enableFireAnimation) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Fixes the firing animation when shot delay is shorter than 4 and weapon is autiomatic
                    if (item.equals(player.getActiveItem()) && weapon.getShotDelay() < 4 && weapon.isAutomatic() && weapon.getBullets() > 0)
                        return;
                    ItemMeta currentMeta = item.getItemMeta();
                    if (currentMeta != null) {
                        currentMeta.setItemModel(NamespacedKey.fromString(model));
                        item.setItemMeta(currentMeta);
                    }
                }
            }.runTaskLater(plugin, 3L);
        }

        player.setCooldown(item, weapon.getShotDelay());

        float randomPitch = (float) ThreadLocalRandom.current().nextDouble(0.93, 1.17);
        playSound(player, "crystalmc:weapon." + weapon.getShootSound(), randomPitch);

        // ----------------------------------------------------------------------------------------------------------

        Map<LivingEntity, Double> damageMap = new HashMap<>();

        for (int i = 0; i < weapon.getBulletsPerShot(); i++) {
            HitResult hit = getHitLocation(player, weapon);

            if (plugin.getConfiguration().weapon.visual.enableBulletTracers) {
                createBulletTracer(player, hit, weapon);
            }
            if (plugin.getConfiguration().weapon.visual.bulletHitDecal.enable) {
                createBulletHitDecal(player, hit, weapon);
            }

            if (weapon.getRecoil() > 0) recoil(player, weapon);

            Vector direction = hit.location().toVector().subtract(player.getEyeLocation().toVector());

            RayTraceResult result = player.getWorld().rayTraceEntities(
                    player.getEyeLocation(),
                    direction,
                    weapon.getMaxDistance(),
                    0.15,
                    entity -> entity != player && entity instanceof LivingEntity && player.hasLineOfSight(entity)
            );

            if (result != null && result.getHitEntity() instanceof LivingEntity entity) {
                damageMap.put(entity, damageMap.getOrDefault(entity, 0.0) + weapon.getDamage());
            }
        }
        // ----------------------------------------------------------------------------------------------------------

        for (Map.Entry<LivingEntity, Double> entry : damageMap.entrySet()) {
            entry.getKey().damage(entry.getValue(), player);
            entry.getKey().setNoDamageTicks(0);
        }
    }

    public HitResult getHitLocation(Player player, Weapon weapon) {
        boolean isStandingStill = player.getVelocity().getX() == 0.0
                && player.getVelocity().getY() <= 0.0
                && player.getVelocity().getZ() == 0.0
                && player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR;

        double inaccuracy = !isStandingStill ? weapon.getInaccuracy() + plugin.getConfiguration().weapon.gameplay.movementInaccuracy : weapon.getInaccuracy();

        Vector vector;
        if (inaccuracy > 0) {
            vector = new Vector(
                    ThreadLocalRandom.current().nextDouble(0.0, inaccuracy) * getRandomFactor(),
                    ThreadLocalRandom.current().nextDouble(0.0, inaccuracy) * getRandomFactor(),
                    ThreadLocalRandom.current().nextDouble(0.0, inaccuracy) * getRandomFactor()
            );
        } else {
            vector = new Vector();
        }

        Vector direction = player.getEyeLocation().clone().getDirection().add(vector);
        Location start = player.getEyeLocation();

        RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(),
                direction,
                weapon.getMaxDistance(),
                FluidCollisionMode.ALWAYS,
                true
        );

        Location location = result != null
                ? result.getHitPosition().toLocation(start.getWorld())
                : start.clone().add(direction.multiply(weapon.getMaxDistance()));

        BlockFace hitFace = result != null ? result.getHitBlockFace() : null;

        return new HitResult(location, hitFace);
    }

    public void recoil(Player player, Weapon weapon) {
        float startYaw = player.getLocation().getYaw();
        float startPitch = player.getLocation().getPitch();

        float yaw = startYaw + (float) (-weapon.getRecoil() / 3.0 * getRandomFactor());
        float pitch = startPitch + (-weapon.getRecoil());

        player.setRotation(yaw, pitch);
    }

    public void createBulletTracer(Player player, HitResult hit, Weapon weapon) {
        Vector direction = player.getEyeLocation().clone().getDirection().multiply(0.25);

        Location start = player.getEyeLocation().add(direction);
        start.setY(start.getY() - 0.05);

        Location hitLocation = hit.location();

        double length = start.distance(hitLocation);

        Location center = start.clone().add(
                hitLocation.clone().subtract(start).multiply(0.5)
        );

        ItemDisplay displayTracer = start.getWorld().spawn(center, ItemDisplay.class);

        Material tracerBlock;
        try {
            tracerBlock = Material.valueOf(weapon.getBulletTracerBlock().toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.sendWarning("Bullet tracer: '" + weapon.getBulletTracerBlock() + "' is not a valid material");
            tracerBlock = Material.GRAY_STAINED_GLASS;
        }

        displayTracer.setItemStack(new ItemStack(tracerBlock));
        displayTracer.setBillboard(Display.Billboard.FIXED);

        displayTracer.setTransformation(new Transformation(
                new Vector3f(),
                new AxisAngle4f(),
                new Vector3f(0.004f, 0.004f, (float) length),
                new AxisAngle4f()
        ));

        new BukkitRunnable() {
            @Override
            public void run() {
                displayTracer.remove();
            }
        }.runTaskLater(plugin, 2L);
    }




    private void createBulletHitDecal(Player player, HitResult hit, Weapon weapon) {
        Vector direction = player.getEyeLocation().clone().getDirection().multiply(0.25);

        Location hitLocation = hit.location();
        BlockFace blockFace = hit.blockFace();
        Location start = player.getEyeLocation().add(direction);
        start.setY(start.getY() - 0.05);

        Block hitBlock = hitLocation.getBlock();

        // check for negative coordinates (closest block)
        if (!hitLocation.getBlock().getType().isSolid()) {
            Location check = hitLocation.clone();
            check.setX(check.getX() - 0.1);
            check.setZ(check.getZ() - 0.1);
            check.setY(check.getY() - 0.1);
            if (!check.getBlock().getType().isSolid()) {
                return;
            } else {
                hitBlock = check.getBlock();
            }
        }

        AxisAngle4f rotation = getAxisAngle4f(blockFace);

        if (!weapon.isSpawnBulletHitDecal()) return;

        ItemDisplay displayHit = start.getWorld().spawn(hitLocation, ItemDisplay.class);

        displayHit.setItemStack(BulletHitDecals.texture);
        displayHit.setBillboard(Display.Billboard.FIXED);

        displayHit.setTransformation(new Transformation(
                new Vector3f(),
                rotation,
                new Vector3f(0.2F, 0.2F, 0.2F),
                new AxisAngle4f()
        ));

        BulletHitDecals.start(displayHit, hitBlock, plugin, plugin.getConfiguration());
    }




    private static @NonNull AxisAngle4f getAxisAngle4f(BlockFace blockFace) {
        AxisAngle4f rotation;
        if (blockFace == BlockFace.UP) {
            rotation = new AxisAngle4f((float) Math.toRadians(90.0), 1f, 0f, 0f);
        } else if (blockFace == BlockFace.DOWN) {
            rotation = new AxisAngle4f((float) Math.toRadians(-90.0), 1f, 0f, 0f);
        } else if (blockFace == BlockFace.EAST) {
            rotation = new AxisAngle4f((float) Math.toRadians(-90.0), 0f, 1f, 0f);
        } else if (blockFace == BlockFace.WEST) {
            rotation = new AxisAngle4f((float) Math.toRadians(90.0), 0f, 1f, 0f);
        } else if (blockFace == BlockFace.SOUTH) {
            rotation = new AxisAngle4f((float) Math.toRadians(180.0), 0f, 1f, 0f);
        } else {
            rotation = new AxisAngle4f();
        }
        return rotation;
    }

    public void displayBullets(Player player, int bullets, int maxBullets) {
        showHotbar(player, "Ammunition: " + bullets + "/" + maxBullets);
    }

    public byte getRandomFactor() {
        return ThreadLocalRandom.current().nextBoolean() ? (byte) 1 : (byte) -1;
    }

    public Weapon getWeaponData(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;

        Weapon[] weaponHolder = new Weapon[1];

        NBT.get(item, nbt -> {
            var compound = nbt.getCompound(plugin.COMPOUND);
            if (compound != null) {
                weaponHolder[0] = NBTUtils.nbtToObject(compound, Weapon.class);
            }
        });

        return weaponHolder[0];
    }

    public record HitResult(Location location, BlockFace blockFace) {}
}