package eu.crystalmc.weapons.config;

import de.exlll.configlib.*;


import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public class Config {

    public Data data;

    YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder().setNameFormatter(NameFormatters.LOWER_UNDERSCORE).build();

    public Config(Path file) {

        this.data = YamlConfigurations.update(file, Data.class, properties);
    }

    public void reload(Path file) {
        this.data = YamlConfigurations.load(file, Data.class, properties);
    }

    public void save(Path file) {
        this.data = YamlConfigurations.update(file, Data.class, properties);
    }




    @Configuration
    public static class Data {

        public Permissions permissions = new Permissions();

        public Weapons weapons = new Weapons();
        public Resourcepack resourcepack = new Resourcepack();


        @Configuration
        public static class Permissions {
            @Comment({
                    "Effects reloading and shooting.",
                    "Set to 'default' to skip permission check."
            })
            public String useWeapon = "default";

            @Comment("Effects entire '/weapon' command.")
            public String command = "crystalweapons.command";

            public String give = "crystalweapons.command.give";
            public String modify = "crystalweapons.command.modify";
            public String save = "crystalweapons.command.save";
            public String removeBulletDecals = "crystalweapons.command.removebulletdecals";


        }

        @Configuration
        public static class Weapons {
            public Visual visual = new Visual();
            public Gameplay gameplay = new Gameplay();
            public Sound sound = new Sound();

            public ConfigurationData data = new ConfigurationData();



            @Configuration
            public static class Visual {
                public Boolean enableBulletTracers = true;
                public Boolean enableFireAnimation = true;

                public String item = "RABBIT_HIDE";

                public BulletHitDecal bulletHitDecal = new BulletHitDecal();

                @Configuration
                public static class BulletHitDecal {
                    public Boolean enable = true;

                    @Comment({
                            "When you set a limit to maximum amount of decals, the plugin will remove the old ones once the limit is reached.",
                            "But if you set it to -1, there won't be a limit. Other than huge lag on client side, this probably won't change much."})
                    public int limit = 500;

                    @Comment("Time in seconds.")
                    public int disappearAfter = 45;

                    @Comment( {
                            "Time in seconds.",
                            "Plugin automatically removes all unused decals (those which weren't created on current runtime) in provided interval."
                    })
                    public int cleanUnusedDecalsInterval = 240;

                }

                @Comment( {
                        "When shot_delay of weapon is less than or equal to this value, the plugin will not change the model of the weapon.",
                        "This fixes the issue where weapon was not visible while shooting."
                })
                public int fireAnimationFix = 5;
            }


            @Configuration
            public static class Sound {
                public Float maxPitch = 1.17F;
                public Float minPitch = 0.93F;
                public Float volume = 1.00F;
            }

            @Configuration
            public static class ConfigurationData {
                public boolean synchronizeWeaponWithConfiguration = true;
            }




            @Configuration
            public static class Gameplay {
                public LimitDistanceForAllWeapons limitDistanceForAllWeapons = new LimitDistanceForAllWeapons();

                @Configuration
                public static class LimitDistanceForAllWeapons {
                    @Comment({
                            "When enabled: every weapon will use this distance limit (number of blocks).",
                            "Please note that there is not hard-coded limit, so if you set this to some high number, the server might lag or crash."
                    })
                    public Boolean enabled = false;
                    public int distance = 128;
                }

                @Comment({
                        "This is an experimental feature.",
                })

                public Double movementInaccuracy = 0.00;

                public Double bulletHitboxSize = 0.10;

            }
        }

        @Configuration
        public static class Resourcepack {
            @Comment({
                    "Can be set to:",
                    "- auto         host locally resourcepack.",
                    "- upload       upload resourcepack to a provided ftp server.",
                    "- provided     does not host or upload resourcepack but sends pack to user from provided address.",
                    "- none         does nothing."
            })
            public String mode = "auto";

            public ModeAuto modeAuto = new ModeAuto();
            public ModeUpload modeUpload = new ModeUpload();
            public ModeProvided modeProvided = new ModeProvided();

            @Configuration
            public static class ModeAuto {
                @Comment("You need to make this port public in-order for other players to download resourcepack.")
                public int port = 80;
                @Comment({
                        "This is your external ip-address, domain or whatever.",
                        "Default: localhost (will only work for you)."
                })
                public String address = "localhost";
            }

            @Configuration
            public static class ModeUpload {
                public String host = "0.0.0.0";
                public String user = "default";
                public String password = "password";
                public String uploadFile = "/resourcepack.zip";
            }

            @Configuration
            public static class ModeProvided {
                public String address = "https://your-website:8080/resourcepack.zip";
            }


            public Set<String> copyResourcepackOutputTo = Collections.emptySet();
        }

    }
}