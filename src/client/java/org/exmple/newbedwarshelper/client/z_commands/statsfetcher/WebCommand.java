package org.exmple.newbedwarshelper.client.z_commands.statsfetcher;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.exmple.newbedwarshelper.ModConstants;
import org.exmple.newbedwarshelper.client.statsfetcher.BWStatsExtractor;
import org.exmple.newbedwarshelper.client.statsfetcher.BWStatsFormatter;
import org.exmple.newbedwarshelper.client.utils.AsyncExecutor;
import org.exmple.newbedwarshelper.client.utils.ServiceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public final class WebCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModConstants.MOD_ID);
    private static final String USAGE_KEY = "commands.newbedwarshelper.web.usage";
    private static final String NICKED_KEY = "commands.newbedwarshelper.statsfetcher.player_may_be_nicked";

    private WebCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("web")
                        .executes(WebCommand::showUsage)
                        .then(ClientCommands.argument("playername", StringArgumentType.string())
                                .suggests(WebCommand::suggestPlayerNames)
                                .executes(WebCommand::fetchAndDisplayStats))));
    }

    private static int showUsage(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().sendFeedback(
                Component.translatable(USAGE_KEY).withStyle(ChatFormatting.RED)
        );
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestPlayerNames(CommandContext<FabricClientCommandSource> ctx, SuggestionsBuilder builder) {
        if (Minecraft.getInstance().getConnection() == null) {
            return builder.buildFuture();
        }

        String inputPrefix = builder.getRemaining().toLowerCase();
        Minecraft.getInstance().getConnection().getListedOnlinePlayers().stream()
                .map(info -> info.getProfile().name())
                .map(name -> ServiceContainer.getNameFormatter().cleanPlayerName(name))
                .filter(name -> name.toLowerCase().startsWith(inputPrefix))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private static int fetchAndDisplayStats(CommandContext<FabricClientCommandSource> ctx) {
        String player = StringArgumentType.getString(ctx, "playername");
        String cleanPlayer = ServiceContainer.getNameFormatter().cleanPlayerName(player);

        CompletableFuture.supplyAsync(() -> fetchStats(cleanPlayer), AsyncExecutor.getExecutor())
                .thenAcceptAsync(stats -> displayStats(ctx, cleanPlayer, stats), Minecraft.getInstance())
                .exceptionally(ex -> handleFetchError(ctx, cleanPlayer, ex));

        return 1;
    }

    private static BWStatsExtractor.BWStats fetchStats(String player) {
        try {
            return ServiceContainer.getStatsExtractor().extractBWStats(player);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void displayStats(CommandContext<FabricClientCommandSource> ctx, String player, BWStatsExtractor.BWStats stats) {
        ctx.getSource().sendFeedback(Component.literal(player + ":").withStyle(ChatFormatting.YELLOW));
        for (Component line : BWStatsFormatter.formatStats(stats)) {
            ctx.getSource().sendFeedback(line);
        }
    }

    private static Void handleFetchError(CommandContext<FabricClientCommandSource> ctx, String player, Throwable exception) {
        LOGGER.error("Failed to fetch stats for {}", player, exception);
        Minecraft.getInstance().execute(() -> {
            ctx.getSource().sendFeedback(Component.literal(player + " :").withStyle(ChatFormatting.YELLOW));
            ctx.getSource().sendFeedback(Component.translatable(NICKED_KEY).withStyle(ChatFormatting.RED));
        });
        return null;
    }
}
