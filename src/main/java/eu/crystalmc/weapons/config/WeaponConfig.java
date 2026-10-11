package eu.crystalmc.weapons.config;

import com.google.gson.annotations.SerializedName;

public class WeaponConfig {

    @SerializedName("item")
    private Item item = new Item();

    @SerializedName("sounds")
    private Sounds sounds = new Sounds();

    @SerializedName("parent_model")
    private String model = "default";

    @SerializedName("max_bullets")
    private int maxBullets = 12;

    @SerializedName("reload_time")
    private int reloadTime = 45;

    @SerializedName("shot_delay")
    private int shotDelay = 4;

    @SerializedName("damage")
    private double damage = 2.0;

    @SerializedName("inaccuracy")
    private double inaccuracy = 0.017;

    @SerializedName("recoil")
    private float recoil = 0.62F;

    @SerializedName("automatic")
    private boolean automatic = true;

    @SerializedName("max_distance")
    private int maxDistance = 58;

    @SerializedName("bullets_per_shot")
    private int bulletsPerShot = 1;

    @SerializedName("infinite_ammo")
    private boolean infiniteAmmo = false;

    @SerializedName("gun_fire_animation_model")
    private String gunFireAnimationModel = "weapon_gun_fire";

    @SerializedName("bullet_tracer_block")
    private String bulletTracerBlock = "GRAY_STAINED_GLASS";

    @SerializedName("spawn_hit_marker")
    private boolean spawnHitMarker = true;

    // for gson to work
    public WeaponConfig() {
    }

    public WeaponConfig(int maxBullets, int reloadTime, int shotDelay, double damage, double inaccuracy,
                        float recoil, boolean automatic, int maxDistance, String shootSound,
                        String reloadSound, int bulletsPerShot, boolean infiniteAmmo,
                        String gunFireAnimationModel, String bulletTracerBlock, boolean spawnHitMarker) {

        this.maxBullets = maxBullets;
        this.reloadTime = reloadTime;
        this.shotDelay = shotDelay;
        this.damage = damage;
        this.inaccuracy = inaccuracy;
        this.recoil = recoil;
        this.automatic = automatic;
        this.maxDistance = maxDistance;
        this.sounds.shoot = shootSound;
        this.sounds.reload = reloadSound;
        this.bulletsPerShot = bulletsPerShot;
        this.infiniteAmmo = infiniteAmmo;
        this.gunFireAnimationModel = gunFireAnimationModel;
        this.bulletTracerBlock = bulletTracerBlock;
        this.spawnHitMarker = spawnHitMarker;
    }

    public int getMaxBullets() {
        return maxBullets;
    }

    public String getDisplayName() {
        return item.displayName;
    }


    public String getItem() {
        return item.item;
    }


    public int getReloadTime() {
        return reloadTime;
    }


    public int getShotDelay() {
        return shotDelay;
    }


    public double getDamage() {
        return damage;
    }


    public double getInaccuracy() {
        return inaccuracy;
    }


    public float getRecoil() {
        return recoil;
    }


    public boolean isAutomatic() {
        return automatic;
    }

    public int getMaxDistance() {
        return maxDistance;
    }


    public String getShootSound() {
        return sounds.shoot;
    }


    public String getReloadSound() {
        return sounds.reload;
    }


    public int getBulletsPerShot() {
        return bulletsPerShot;
    }


    public boolean isInfiniteAmmo() {
        return infiniteAmmo;
    }


    public String getGunFireAnimationModel() {
        return gunFireAnimationModel;
    }

    public String getModel() {
        return model;
    }


    public String getBulletTracerBlock() {
        return bulletTracerBlock;
    }

    public boolean isSpawnHitMarker() {
        return spawnHitMarker;
    }



    public static class Item {
        Item() {}

        @SerializedName("display_name")
        private String displayName = "Weapon";

        @SerializedName("item")
        private String item = "PAPER";
    }

    public static class Sounds {
        Sounds() {}

        @SerializedName("shoot")
        private String shoot = "weapon.shoot";

        @SerializedName("reload")
        private String reload = "weapon.reload";
    }

}