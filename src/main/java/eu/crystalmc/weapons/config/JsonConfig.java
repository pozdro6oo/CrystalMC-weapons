package eu.crystalmc.weapons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


public class JsonConfig<T> {
    private final File file;
    public T config;
    private final Class<T> configClass;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public JsonConfig(File file, Class<T> configClass) {
        this.file = file;
        this.configClass = configClass;
        load();
    }

   public boolean load() {
        if (!file.exists()) {
            try {
                config = configClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return false;
            }
            save();
            return true;
        }
       try {
           String raw = Files.readString(file.toPath(), StandardCharsets.UTF_8);
           config = configClass.getDeclaredConstructor().newInstance();
           config = gson.fromJson(raw, configClass);
           return true;
       } catch (Exception e) {
           return false;
       }
    }

    public boolean save(File file) {
        file.getParentFile().mkdirs();
        try {
            Files.writeString(file.toPath(), gson.toJson(config), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean save() {
        return save(this.file);
    }


}