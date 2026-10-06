package eu.crystalmc.weapons.config;

public interface JsonConfig<T : class> {
    File file;
    static class config;


    fun load() {
        if (!file.exists()) {
            save()
            return
        }

        config = gson.fromJson(
                file.readText(),
                config:: class.java
            )
    }

    fun save() {
        file.parentFile ?.mkdirs()

        file.writeText(
                gson.toJson(config)
        )
    }

    fun merge(jsonFile:File) {
        val thisJson = JSONUtils.getJSON(file)
        val secondJson = JSONUtils.getJSON(jsonFile)
        for (entry in secondJson.entrySet()) {
            if (!thisJson.has(entry.key)) {
                thisJson.add(entry.key, entry.value)
            }
        }
        config = gson.fromJson(thisJson, config:: class.java)
    }
}


class SimpleJsonConfig<T: Json>(override val file: File, override var config: T) : JsonConfig<T>

private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()