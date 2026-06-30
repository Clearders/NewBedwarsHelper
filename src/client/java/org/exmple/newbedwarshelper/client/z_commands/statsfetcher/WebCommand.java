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
import net.minecraft.network.chat.MutableComponent;
import org.exmple.newbedwarshelper.ModConstants;
import org.exmple.newbedwarshelper.client.statsfetcher.BWStatsExtractor;
import org.exmple.newbedwarshelper.client.statsfetcher.BWStatsFormatter;
import org.exmple.newbedwarshelper.client.statsfetcher.StatsCopyComponents;
import org.exmple.newbedwarshelper.client.utils.AsyncExecutor;
import org.exmple.newbedwarshelper.client.utils.ServiceContainer;
import org.exmple.newbedwarshelper.client.utils.bedwars.BedwarsRosterResolver;
import org.exmple.newbedwarshelper.client.utils.bedwars.BedwarsTeamMarker;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;
import org.jsoup.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;

public final class WebCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModConstants.MOD_ID);
    private static final long RATE_LIMIT_RETRY_DELAY_MS = 2600L;
    private static final String USAGE_KEY = "commands.newbedwarshelper.web.usage";
    private static final String NICKED_KEY = "commands.newbedwarshelper.statsfetcher.player_may_be_nicked";
    private static final String RATE_LIMIT_KEY = "commands.newbedwarshelper.statsfetcher.website_rate_limited";
    private static final String NETWORK_ERROR_KEY = "commands.newbedwarshelper.statsfetcher.network_error";
    private static final String PLAYER_NOT_FOUND_KEY = "commands.newbedwarshelper.statsfetcher.player_not_found";
    private static final String NO_STATS_FIELDS_ENABLED_KEY = "commands.newbedwarshelper.statsfetcher.no_stats_fields_enabled";

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
        if (!BWStatsFormatter.hasAnyDisplayFieldEnabled(ModConfig.getInstance().statsFetcher)) {
            ctx.getSource().sendFeedback(Component.translatable(NO_STATS_FIELDS_ENABLED_KEY).withStyle(ChatFormatting.RED));
            return 0;
        }

        String player = StringArgumentType.getString(ctx, "playername");
        String cleanPlayer = ServiceContainer.getNameFormatter().cleanPlayerName(player);
        QueryContext queryContext = createQueryContext(cleanPlayer);

        CompletableFuture.supplyAsync(() -> fetchStatsWithRetry(cleanPlayer), AsyncExecutor.getExecutor())
                .thenAcceptAsync(stats -> displayStats(ctx, queryContext, stats), Minecraft.getInstance())
                .exceptionally(ex -> handleFetchError(ctx, queryContext, ex));

        return 1;
    }

    private static QueryContext createQueryContext(String player) {
        BedwarsRosterResolver.Snapshot rosterSnapshot = BedwarsRosterResolver.resolve(Minecraft.getInstance());
        if (!rosterSnapshot.available()) {
            return new QueryContext(player, Optional.empty(), false);
        }

        Optional<String> rosterName = rosterSnapshot.playerOrder().stream()
                .filter(name -> name.equalsIgnoreCase(player))
                .findFirst();
        if (rosterName.isEmpty()) {
            return new QueryContext(player, Optional.empty(), false);
        }

        Optional<BedwarsTeamMarker> marker = rosterSnapshot.teamForPlayer(rosterName.get());
        return new QueryContext(rosterName.get(), marker, true);
    }

    private static BWStatsExtractor.BWStats fetchStatsWithRetry(String player) {
        try {
            return ServiceContainer.getStatsExtractor().extractBWStats(player);
        } catch (Exception exception) {
            if (isRateLimited(exception)) {
                try {
                    Thread.sleep(RATE_LIMIT_RETRY_DELAY_MS);
                    return ServiceContainer.getStatsExtractor().extractBWStats(player);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(interruptedException);
                } catch (Exception retryException) {
                    throw new RuntimeException(retryException);
                }
            }
            throw new RuntimeException(exception);
        }
    }

    private static void displayStats(CommandContext<FabricClientCommandSource> ctx, QueryContext queryContext, BWStatsExtractor.BWStats stats) {
        Component header = createPlayerHeader(queryContext, false);
        if (Boolean.TRUE.equals(ModConfig.getInstance().statsFetcher.copyButtonsEnabled)) {
            header = StatsCopyComponents.appendCopyButton(header, buildCopyText(queryContext.name(), stats));
        }

        ctx.getSource().sendFeedback(header);
        for (Component line : BWStatsFormatter.formatStats(stats)) {
            ctx.getSource().sendFeedback(line);
        }
    }

    private static String buildCopyText(String player, BWStatsExtractor.BWStats stats) {
        String statsText = BWStatsFormatter.formatStatsSummaryText(stats);
        return statsText.isBlank() ? player + ":" : player + ": " + statsText;
    }

    private static MutableComponent createPlayerHeader(QueryContext queryContext, boolean nickedStyle) {
        ChatFormatting nameColor = queryContext.marker().map(BedwarsTeamMarker::color).orElse(ChatFormatting.YELLOW);
        return Component.empty()
                .append(Component.literal(queryContext.name()).withStyle(style -> {
                    style = style.withColor(nameColor);
                    if (nickedStyle && queryContext.inCurrentGame()) {
                        style = style.withItalic(true).withUnderlined(true);
                    }
                    return style;
                }))
                .append(Component.literal(":").withStyle(nameColor));
    }

    private static Void handleFetchError(CommandContext<FabricClientCommandSource> ctx, QueryContext queryContext, Throwable exception) {
        Throwable cause = unwrap(exception);
        LOGGER.error("Failed to fetch stats for {}", queryContext.name(), cause);
        Minecraft.getInstance().execute(() -> {
            FailureMessage failureMessage = classifyFailure(queryContext, cause);
            Component header = createPlayerHeader(queryContext, failureMessage.nickedStyle());
            if (Boolean.TRUE.equals(ModConfig.getInstance().statsFetcher.copyButtonsEnabled)) {
                header = StatsCopyComponents.appendCopyButton(header, buildStatusCopyText(queryContext.name(), failureMessage.copyText()));
            }
            ctx.getSource().sendFeedback(header);
            ctx.getSource().sendFeedback(Component.translatable(failureMessage.messageKey()).withStyle(ChatFormatting.RED));
        });
        return null;
    }

    private static FailureMessage classifyFailure(QueryContext queryContext, Throwable exception) {
        if (isRateLimited(exception)) {
            return new FailureMessage(RATE_LIMIT_KEY, false, getCopyText(RATE_LIMIT_KEY, "Fetch failed because of website rate limit."));
        }

        if (isNetworkError(exception)) {
            return new FailureMessage(NETWORK_ERROR_KEY, false, getCopyText(NETWORK_ERROR_KEY, "Fetch failed because of network error."));
        }

        if (queryContext.inCurrentGame()) {
            return new FailureMessage(NICKED_KEY, true, "nicked");
        }

        return new FailureMessage(PLAYER_NOT_FOUND_KEY, false, getCopyText(PLAYER_NOT_FOUND_KEY, "This player doesn't exist!"));
    }

    private static String buildStatusCopyText(String player, String statusText) {
        return player + ": " + statusText;
    }

    private static String getCopyText(String key, String englishText) {
        if (Boolean.TRUE.equals(ModConfig.getInstance().statsFetcher.copyTextInEnglish)) {
            return englishText;
        }

        return Component.translatable(key).getString();
    }

    private static boolean isRateLimited(Throwable exception) {
        return exception instanceof HttpStatusException httpStatusException && httpStatusException.getStatusCode() == 429;
    }

    private static boolean isNetworkError(Throwable exception) {
        return exception instanceof IOException && !(exception instanceof HttpStatusException);
    }

    private static Throwable unwrap(Throwable exception) {
        Throwable current = exception;
        while ((current instanceof CompletionException || current instanceof RuntimeException) && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private record QueryContext(String name, Optional<BedwarsTeamMarker> marker, boolean inCurrentGame) {
    }

    private record FailureMessage(String messageKey, boolean nickedStyle, String copyText) {
    }
}
