package sidly.soundadjuster;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.util.tuples.Pair;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class SoundAdjuster implements ClientModInitializer {
    public static final String MOD_ID = "soundadjuster";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    protected static final Registry<@NotNull SoundEvent> SOUND_REGISTRY = BuiltInRegistries.SOUND_EVENT;
    private static final Map<Identifier, Float> soundMultipliers = new HashMap<>();

    private static final Queue<Pair<Identifier, Long>> soundQueue = new ArrayDeque<>(200);
    private static Identifier lastAdjusted = null;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(Commands::registerCommands);
        Map<Identifier, Float> config = Config.load();
        if (config != null) soundMultipliers.putAll(config);
    }

    public static float getVolumeMultiplier(Identifier id) {
        soundQueue.add(new Pair<>(id, System.currentTimeMillis()));
        return soundMultipliers.getOrDefault(id, 1.0F);
    }

    public static Map<Identifier, Float> getVolumeMultipliers() {
        return soundMultipliers;
    }

    public static boolean adjustVolume(Identifier id, float multiplier) {
        lastAdjusted = id;
        soundMultipliers.put(id, multiplier);
        Config.save();
        return true;
    }

    public static boolean adjustVolume(String name, float multiplier) {
        Identifier id = Identifier.tryParse(name);
        if (id == null) return false;
        return adjustVolume(id, multiplier);
    }

    public static Queue<Pair<Identifier, Long>> getSoundQueue() {
        return soundQueue;
    }

    public static Identifier getLastAdjusted() {
        return lastAdjusted;
    }
}