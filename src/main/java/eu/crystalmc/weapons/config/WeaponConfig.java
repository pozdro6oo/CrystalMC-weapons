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

    @SerializedName("is_automatic")
    private boolean isAutomatic = true;

    @SerializedName("max_distance")
    private int maxDistance = 58;

    @SerializedName("bullets_per_shot")
    private int bulletsPerShot = 1;

    @SerializedName("infinite_ammo")
    private boolean infiniteAmmo = false;

    @SerializedName("gun_fire_animation_model")
    private String gunFireAnimationModel = "default";

    @SerializedName("bullet_tracer_block")
    private String bulletTracerBlock = "GRAY_STAINED_GLASS";

    @SerializedName("spawn_bullet_hit_decal")
    private boolean spawnBulletHitDecal = true;

    // for gson to work
    public WeaponConfig() {
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
        return isAutomatic;
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

    public boolean isSpawnBulletHitDecal() {
        return spawnBulletHitDecal;
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