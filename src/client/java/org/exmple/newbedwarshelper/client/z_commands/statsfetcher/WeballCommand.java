package org.exmple.newbedwarshelper.client.z_commands.statsfetcher;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
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
import org.exmple.newbedwarshelper.client.utils.RateLimiter;
import org.exmple.newbedwarshelper.client.utils.ServiceContainer;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class WeballCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModConstants.MOD_ID);
    private static final RateLimiter RATE_LIMITER = new RateLimiter(2, 1.0, 800);
    private static final double DEFAULT_DANGEROUS_PLAYERS_KD_THRESHOLD = 1.0;

    private static final String THRESHOLD_CURRENT_KEY = "commands.newbedwarshelper.weball.threshold.current";
    private static final String THRESHOLD_INVALID_KEY = "commands.newbedwarshelper.weball.threshold.invalid";
    private static final String THRESHOLD_SET_KEY = "commands.newbedwarshelper.weball.threshold.set";
    private static final String THRESHOLD_RESET_KEY = "commands.newbedwarshelper.weball.threshold.reset";
    private static final String SUMMARY_KEY = "commands.newbedwarshelper.weball.summary";
    private static final String DANGEROUS_PLAYERS_KEY = "commands.newbedwarshelper.weball.dangerous_players";
    private static final String FAILED_KEY = "commands.newbedwarshelper.weball.failed";
    private static final String NICKED_KEY = "commands.newbedwarshelper.statsfetcher.player_may_be_nicked";

    private WeballCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("weball")
                        .executes(WeballCommand::execute)
                        .then(ClientCommands.literal("config")
                                .then(ClientCommands.literal("Dangerous_Players_Final_KD")
                                        .then(ClientCommands.literal("get")
                                                .executes(WeballCommand::handleGetCommand))
                                        .then(ClientCommands.literal("set")
                                                .then(ClientCommands.argument("threshold", DoubleArgumentType.doubleArg(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY))
                                                        .executes(WeballCommand::handleSetCommand)))
                                        .then(ClientCommands.literal("reset")
                                                .executes(WeballCommand::handleResetCommand))))));
    }

    private static int handleGetCommand(CommandContext<FabricClientCommandSource> ctx) {
        double currentThreshold = ModConfig.getInstance().statsFetcher.dangerousPlayersKDThreshold;
        ctx.getSource().sendFeedback(
                Component.translatable(THRESHOLD_CURRENT_KEY)
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.valueOf(currentThreshold)).withStyle(ChatFormatting.YELLOW))
        );
        return 1;
    }

    private static int handleSetCommand(CommandContext<FabricClientCommandSource> ctx) {
        double threshold = DoubleArgumentType.getDouble(ctx, "threshold");

        if (threshold <= 0.0) {
            ctx.getSource().sendFeedback(
                    Component.translatable(THRESHOLD_INVALID_KEY).withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        ModConfig config = ModConfig.getInstance();
        config.statsFetcher.dangerousPlayersKDThreshold = threshold;
        config.save();

        ctx.getSource().sendFeedback(
                Component.translatable(THRESHOLD_SET_KEY)
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.valueOf(threshold)).withStyle(ChatFormatting.YELLOW))
        );

        return 1;
    }

    private static int handleResetCommand(CommandContext<FabricClientCommandSource> ctx) {
        ModConfig config = ModConfig.getInstance();
        config.statsFetcher.dangerousPlayersKDThreshold = DEFAULT_DANGEROUS_PLAYERS_KD_THRESHOLD;
        config.save();

        ctx.getSource().sendFeedback(
                Component.translatable(THRESHOLD_RESET_KEY)
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.valueOf(DEFAULT_DANGEROUS_PLAYERS_KD_THRESHOLD)).withStyle(ChatFormatting.YELLOW))
        );

        return 1;
    }

    private static int execute(CommandContext<FabricClientCommandSource> ctx) {
        if (Minecraft.getInstance().getConnection() == null) {
            return 0;
        }

        List<String> names = Minecraft.getInstance().getConnection().getListedOnlinePlayers().stream()
                .map(info -> ServiceContainer.getNameFormatter().cleanPlayerName(info.getProfile().name()))
                .collect(Collectors.toList());

        CompletableFuture.supplyAsync(() -> processPlayers(ctx, names), AsyncExecutor.getExecutor())
                .thenAcceptAsync(result -> displaySummary(ctx, result), Minecraft.getInstance())
                .exceptionally(ex -> handleError(ctx, ex));

        return 1;
    }

    private static FetchResult processPlayers(CommandContext<FabricClientCommandSource> ctx, List<String> names) {
        long startNano = System.nanoTime();
        List<PlayerKD> dangerous = new ArrayList<>();
        int total = names.size();

        for (int i = 0; i < total; i++) {
            String name = names.get(i);
            int current = i + 1;

            try {
                RATE_LIMITER.consume();
                processSinglePlayer(ctx, name, current, total, dangerous);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception exception) {
                handlePlayerError(ctx, name, current, total, dangerous);
            }
        }

        double elapsedSeconds = (System.nanoTime() - startNano) / 1_000_000_000.0;
        return new FetchResult(elapsedSeconds, dangerous);
    }

    private static void processSinglePlayer(CommandContext<FabricClientCommandSource> ctx, String name, int current, int total, List<PlayerKD> dangerous) throws Exception {
        BWStatsExtractor.BWStats stats = ServiceContainer.getStatsExtractor().extractBWStats(name);
        addDangerousPlayerIfNeeded(name, stats, dangerous);

        Minecraft.getInstance().execute(() -> {
            Component header = Component.literal(name + " ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("[" + current + "/" + total + "]:").withStyle(ChatFormatting.GOLD));
            ctx.getSource().sendFeedback(header);

            for (Component line : BWStatsFormatter.formatStats(stats)) {
                ctx.getSource().sendFeedback(line);
            }
        });
    }

    private static void addDangerousPlayerIfNeeded(String name, BWStatsExtractor.BWStats stats, List<PlayerKD> dangerous) {
        String kdValue = stats.getFinalKD();
        if (kdValue == null || BWStatsExtractor.isNotFound(kdValue)) {
            return;
        }

        double kdVal = BWStatsFormatter.parseStatAsDouble(kdValue);
        double threshold = ModConfig.getInstance().statsFetcher.dangerousPlayersKDThreshold;
        if (kdVal > threshold) {
            dangerous.add(new PlayerKD(name, kdValue));
        }
    }

    private static void handlePlayerError(CommandContext<FabricClientCommandSource> ctx, String name, int current, int total, List<PlayerKD> dangerous) {
        dangerous.add(new PlayerKD(name, "nicked"));
        Minecraft.getInstance().execute(() -> {
            Component header = Component.literal(name + " ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("[" + current + "/" + total + "]:").withStyle(ChatFormatting.GOLD));
            ctx.getSource().sendFeedback(header);
            ctx.getSource().sendFeedback(Component.translatable(NICKED_KEY).withStyle(ChatFormatting.RED));
        });
    }

    private static void displaySummary(CommandContext<FabricClientCommandSource> ctx, FetchResult result) {
        ctx.getSource().sendFeedback(
                Component.translatable(SUMMARY_KEY, String.format("%.3f", result.elapsedSeconds()))
                        .withStyle(ChatFormatting.AQUA)
        );

        if (ModConfig.getInstance().statsFetcher.showDangerousPlayers && result.dangerous() != null && !result.dangerous().isEmpty()) {
            ctx.getSource().sendFeedback(Component.translatable(DANGEROUS_PLAYERS_KEY).withStyle(ChatFormatting.RED));
            for (PlayerKD playerKD : result.dangerous()) {
                Component component = Component.literal(playerKD.name()).withStyle(ChatFormatting.RED)
                        .append(Component.literal(" (" + playerKD.kd() + ")").withStyle(ChatFormatting.WHITE));
                ctx.getSource().sendFeedback(component);
            }
        }
    }

    private static Void handleError(CommandContext<FabricClientCommandSource> ctx, Throwable exception) {
        LOGGER.error("Weball execution failed", exception);
        Minecraft.getInstance().execute(() -> ctx.getSource().sendFeedback(
                Component.translatable(FAILED_KEY, exception.getMessage()).withStyle(ChatFormatting.RED)
        ));
        return null;
    }

    private record FetchResult(double elapsedSeconds, List<PlayerKD> dangerous) {
    }

    private record PlayerKD(String name, String kd) {
    }
}
