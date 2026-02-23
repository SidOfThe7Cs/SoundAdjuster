package sidly.soundadjuster;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Commands {
    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(adjustCommand());
        dispatcher.register(getAdjustedCommand());
        dispatcher.register(getRecentCommand());
        dispatcher.register(playCommand());
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> adjustCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> base = ClientCommandManager.literal("adjustSoundVolume")
                .executes(context -> {
                    Utils.sendClientChatMessage("No sound specified");
                    return 0;
                });

        RequiredArgumentBuilder<FabricClientCommandSource, String> arg1 = ClientCommandManager.argument("id", StringArgumentType.string())
                .suggests(Commands::soundSelector)
                .executes((context) -> {
                    Utils.sendClientChatMessage("No multiplier specified");
                    return 0;
                });

        RequiredArgumentBuilder<FabricClientCommandSource, Float> arg2 = ClientCommandManager.argument("multiplier", FloatArgumentType.floatArg(0.0F))
                .executes(context -> {
                    String soundID = StringArgumentType.getString(context, "id");
                    float multiplier = FloatArgumentType.getFloat(context, "multiplier");
                    if (SoundAdjuster.adjustVolume(soundID, multiplier)) {
                        Utils.sendClientChatMessage("Set volume multiplier for " + soundID + " to " + multiplier);
                        return 1;
                    }
                    Utils.sendClientChatMessage("Failed");
                    return 0;
                });

        return base.then(arg1.then(arg2));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> getAdjustedCommand() {
        return ClientCommandManager.literal("getAdjustedSounds")
                .executes(context -> {
                    StringBuilder sb = new StringBuilder();
                    SoundAdjuster.getVolumeMultipliers().entrySet().stream()
                            .filter(entry -> entry.getValue() != 1)
                            .sorted(Map.Entry.comparingByValue())
                            .forEach(entry -> sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));
                    if (sb.isEmpty()) sb.append("No adjusted sounds");
                    Utils.sendClientChatMessage(sb.toString());
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> getRecentCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> base = ClientCommandManager.literal("getRecentSounds")
                .executes(context -> {
                    displaySoundQueue(50);
                    return 1;
                });

        RequiredArgumentBuilder<FabricClientCommandSource, Integer> arg1 = ClientCommandManager.argument("count", IntegerArgumentType.integer(1, 200))
                .executes(context -> {
                    int count = IntegerArgumentType.getInteger(context, "count");
                    displaySoundQueue(count);
                    return 1;
                });
        
        return base.then(arg1);
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> playCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> base = ClientCommandManager.literal("playSoundClient")
                .executes(context -> {
                    Identifier id = SoundAdjuster.getLastAdjusted();
                    if (id != null) {
                        Utils.playSound(id);
                    } else Utils.sendClientChatMessage("No adjusted sounds since launch");
                    return 0;
                });

        RequiredArgumentBuilder<FabricClientCommandSource, String> arg1 = ClientCommandManager.argument("id", StringArgumentType.string())
                .suggests(Commands::soundSelector)
                .executes(context -> {
                    String soundID = StringArgumentType.getString(context, "id");
                    Utils.playSound(soundID);
                    return 1;
                });

        return base.then(arg1);
    }

    private static CompletableFuture<Suggestions> soundSelector(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        return soundSelector(builder);
    }

    private static CompletableFuture<Suggestions> soundSelector(SuggestionsBuilder builder) {
        String userInput = builder.getRemaining().toLowerCase().replace('"', ' ').trim();
        SoundAdjuster.SOUND_REGISTRY.keySet().stream()
                .map(Identifier::toString)
                .filter(id -> id.toLowerCase().contains(userInput))
                .forEach(id -> builder.suggest('"' + id + '"'));
        return builder.buildFuture();
    }
    
    private static void displaySoundQueue(int count) {
        StringBuilder sb = new StringBuilder();
        SoundAdjuster.getSoundQueue().stream()
                .skip(Math.max(0, SoundAdjuster.getSoundQueue().size() - count))
                .forEach(entry -> {
                    sb.append(entry.getA()).append(" ");
                    sb.append(Utils.getFormattedTimeSince(entry.getB()));
                    sb.append(" ago\n");
                });
        if (sb.isEmpty()) sb.append("No recent sounds");
        Utils.sendClientChatMessage(sb.toString());
    }
}
