package sidly.soundadjuster;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.time.temporal.ChronoUnit;

public class Utils {
    public static void sendClientChatMessage(String message) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player != null && message != null && !message.isEmpty()) {
            player.displayClientMessage(Component.literal(message), false);
        }
    }

    public static String getFormattedTimeSince(long epoch) {
        return Utils.formatTime(timeSince(epoch), ChronoUnit.MILLIS);
    }

    public static long timeSince(long epoch) {
        return System.currentTimeMillis() - epoch;
    }

    public static String formatTime(long time, ChronoUnit unit) {
        double seconds = unit.getDuration().toMillis() / 1000.0 * Math.abs(time);
        return formatTime(seconds, ChronoUnit.SECONDS);
    }

    public static String formatTime(double time, ChronoUnit unit) {
        double seconds = unit.getDuration().toMillis() / 1000.0 * Math.abs(time);
        String base;

        if (seconds < 60) {
            base = String.format("%.1fs", seconds);
        } else if ((seconds /= 60.0) < 60) {
            base = String.format("%.1fm", seconds);
        } else if ((seconds /= 60.0) < 24) {
            base = String.format("%.1fh", seconds);
        } else if ((seconds /= 24.0) < 7) {
            base = String.format("%.1fd", seconds);
        } else if ((seconds /= 7.0) < 4.345) {
            base = String.format("%.1fw", seconds);
        } else if ((seconds *= 7.0 / 30.4375) < 12) {
            base = String.format("%.1fmo", seconds);
        } else {
            base = String.format("%.1fy", seconds / 12.0);
        }

        return base;
    }

    public static void playSound(String name) {
        Identifier id = Identifier.tryParse(name);
        if (id == null) return;
        playSound(id);
    }

    public static void playSound(Identifier id) {
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(id);
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        client.player.playSound(soundEvent);
        sendClientChatMessage("Playing sound: " + id);
    }
}
