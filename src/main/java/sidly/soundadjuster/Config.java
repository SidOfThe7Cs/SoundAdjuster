package sidly.soundadjuster;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class Config {
    public static final File CONFIG_FILE = new File("config/SoundAdjuster.json");
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Identifier.class, (JsonDeserializer<Identifier>) (json, typeOfT, ctx) -> {
                if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                    String fullyQualifiedName = json.getAsString();
                    String[] split = fullyQualifiedName.split(":");
                    if (split.length != 2) throw new JsonParseException("Unexpected JSON for Identifier: " + json);
                    String nameSpace = split[0];
                    String path = split[1];
                    return Identifier.fromNamespaceAndPath(nameSpace, path);
                } else {
                    throw new JsonParseException("Unexpected JSON for Identifier: " + json);
                }
            })
            .create();


    public static Map<Identifier, Float> load() {
        if (!CONFIG_FILE.getParentFile().exists()) CONFIG_FILE.getParentFile().mkdirs();
        try {
            if (CONFIG_FILE.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(CONFIG_FILE.toPath()), StandardCharsets.UTF_8));
                Type type = new TypeToken<@NotNull Map<Identifier, Float>>() {
                }.getType();
                return GSON.fromJson(reader, type);
            } else CONFIG_FILE.createNewFile();
        } catch (IOException e) {
            SoundAdjuster.LOGGER.error("Failed to load config file: {}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static void save() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(CONFIG_FILE.toPath()), StandardCharsets.UTF_8));
            writer.write(GSON.toJson(SoundAdjuster.getVolumeMultipliers()));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            SoundAdjuster.LOGGER.error("Failed to save config file: {}", e.getMessage());
            e.printStackTrace();
        }

    }
}
