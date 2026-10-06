package eu.crystalmc.weapons;

import com.google.gson.annotations.SerializedName;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import eu.crystalmc.weapons.config.WeaponConfig;

public class Weapon extends WeaponConfig {

    private int bullets;

    @SerializedName("model_name")
    private String modelName;

    public Weapon(int bullets, String modelName, int maxBullets, int reloadTime, int shotDelay,
                  double damage, double inaccuracy, float recoil, boolean automatic,
                  int maxDistance, String shootSound, String reloadSound, int bulletsPerShot,
                  boolean infiniteAmmo, String gunFireAnimationModel, String bulletTracerBlock,
                  boolean spawnHitMarker) {

        super(maxBullets, reloadTime, shotDelay, damage, inaccuracy, recoil, automatic,
                maxDistance, shootSound, reloadSound, bulletsPerShot, infiniteAmmo,
                gunFireAnimationModel, bulletTracerBlock, spawnHitMarker);

        this.bullets = bullets;
        this.modelName = modelName;
    }

    public Weapon(WeaponConfig data, String name) {
        this(
                data.getMaxBullets(),
                "crystalmc:weapon/" + name,
                data.getMaxBullets(),
                data.getReloadTime(),
                data.getShotDelay(),
                data.getDamage(),
                data.getInaccuracy(),
                data.getRecoil(),
                data.isAutomatic(),
                data.getMaxDistance(),
                data.getShootSound(),
                data.getReloadSound(),
                data.getBulletsPerShot(),
                data.isInfiniteAmmo(),
                data.getGunFireAnimationModel(),
                data.getBulletTracerBlock(),
                data.isSpawnHitMarker()
        );
    }

    public Weapon(ReadableNBT compound) {
        this(
                compound.getInteger("bullets"),
                compound.getString("model_name"),
                compound.getInteger("max_bullets"),
                compound.getInteger("reload_time"),
                compound.getInteger("shot_delay"),
                compound.getDouble("damage"),
                compound.getDouble("inaccuracy"),
                compound.getFloat("recoil"),
                compound.getBoolean("automatic"),
                compound.getInteger("max_distance"),
                compound.getString("shoot_sound"),
                compound.getString("reload_sound"),
                compound.getInteger("bullets_per_shot"),
                compound.getBoolean("infinite_ammo"),
                compound.getString("gun_fire_animation_model"),
                compound.getString("bullet_tracer_block"),
                compound.getBoolean("spawn_hit_marker")
        );
    }

    public int getBullets() {
        return bullets;
    }

    public void setBullets(int bullets) {
        this.bullets = bullets;
    }

    public String getModelName() {
        return modelName;
    }
}