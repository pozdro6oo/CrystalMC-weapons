package eu.crystalmc.weapons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    public JsonConfig(File file, Class<T> configClass, T config) {
        this.file = file;
        this.configClass = configClass;
        this.config = config;
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
           config = fromString(raw);
           return true;
       } catch (Exception e) {
           return false;
       }
    }

    public void save(File file) {
        file.getParentFile().mkdirs();
        try {
            Files.writeString(file.toPath(), gson.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException _) {
        }
    }

    public void save() {
        save(this.file);
    }

    // This function return


    public T fromString(String raw) {
        return gson.fromJson(raw, configClass);
    }


}