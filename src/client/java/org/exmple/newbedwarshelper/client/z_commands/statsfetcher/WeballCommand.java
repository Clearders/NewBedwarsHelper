package org.exmple.newbedwarshelper.client.z_commands.statsfetcher;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
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
import org.exmple.newbedwarshelper.client.utils.RateLimiter;
import org.exmple.newbedwarshelper.client.utils.ServiceContainer;
import org.exmple.newbedwarshelper.client.utils.bedwars.BedwarsRosterResolver;
import org.exmple.newbedwarshelper.client.utils.bedwars.BedwarsTeamMarker;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jsoup.HttpStatusException;

public final class WeballCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModConstants.MOD_ID);
    private static final RateLimiter RATE_LIMITER = new RateLimiter(1, 1.0 / 1.4, 1300);
    private static final double DEFAULT_DANGEROUS_PLAYERS_KD_THRESHOLD = 1.0;
    private static final long RATE_LIMIT_RETRY_DELAY_MS = 2600L;

    private static final String THRESHOLD_CURRENT_KEY = "commands.newbedwarshelper.weball.threshold.current";
    private static final String THRESHOLD_INVALID_KEY = "commands.newbedwarshelper.weball.threshold.invalid";
    private static final String THRESHOLD_SET_KEY = "commands.newbedwarshelper.weball.threshold.set";
    private static final String THRESHOLD_RESET_KEY = "commands.newbedwarshelper.weball.threshold.reset";
    private static final String SUMMARY_KEY = "commands.newbedwarshelper.weball.summary";
    private static final String DANGEROUS_PLAYERS_KEY = "commands.newbedwarshelper.weball.dangerous_players";
    private static final String FAILED_KEY = "commands.newbedwarshelper.weball.failed";
    private static final String NICKED_KEY = "commands.newbedwarshelper.statsfetcher.player_may_be_nicked";
    private static final String RATE_LIMIT_KEY = "commands.newbedwarshelper.statsfetcher.website_rate_limited";
    private static final String NETWORK_ERROR_KEY = "commands.newbedwarshelper.statsfetcher.network_error";
    private static final String COULD_NOT_FETCH_KEY = "commands.newbedwarshelper.statsfetcher.could_not_fetch";
    private static final String NO_DANGEROUS_PLAYER_KEY = "commands.newbedwarshelper.weball.no_dangerous_player";
    private static final String NO_VISIBLE_OUTPUT_KEY = "commands.newbedwarshelper.weball.no_visible_output_enabled";

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
        return runWeball(ctx.getSource()::sendFeedback) ? 1 : 0;
    }

    public static boolean executeAutoWeball(Minecraft client) {
        if (client.player == null) {
            return false;
        }

        return runWeball(component -> client.gui.chatListener().handleSystemMessage(component, false));
    }

    private static boolean runWeball(Consumer<Component> feedback) {
        if (Minecraft.getInstance().getConnection() == null) {
            return false;
        }

        ModConfig.StatsFetcherConfig config = ModConfig.getInstance().statsFetcher;
        if (!BWStatsFormatter.hasAnyDisplayFieldEnabled(config) && !config.showDangerousPlayers) {
            feedback.accept(Component.translatable(NO_VISIBLE_OUTPUT_KEY).withStyle(ChatFormatting.RED));
            return false;
        }

        List<String> names = collectListedPlayerNames();
        BedwarsRosterResolver.Snapshot rosterSnapshot = BedwarsRosterResolver.resolve(Minecraft.getInstance());
        FetchPlan fetchPlan = buildFetchPlan(names, rosterSnapshot, config);
        runFetchPlan(feedback, fetchPlan);

        return true;
    }

    private static List<String> collectListedPlayerNames() {
        if (Minecraft.getInstance().getConnection() == null) {
            return List.of();
        }

        return Minecraft.getInstance().getConnection().getListedOnlinePlayers().stream()
                .map(info -> ServiceContainer.getNameFormatter().cleanPlayerName(info.getProfile().name()))
                .collect(Collectors.toList());
    }

    private static FetchPlan buildFetchPlan(
            List<String> names,
            BedwarsRosterResolver.Snapshot rosterSnapshot,
            ModConfig.StatsFetcherConfig config
    ) {
        List<FetchTarget> targets = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            FetchTarget target = new FetchTarget(
                    name,
                    rosterSnapshot.available() ? rosterSnapshot.teamForPlayer(name) : Optional.empty(),
                    i
            );
            if (shouldFetchTarget(target, rosterSnapshot, config)) {
                targets.add(target);
            }
        }
        if (rosterSnapshot.available()) {
            sortTargetsByTeamOrder(targets, rosterSnapshot.teamOrder());
        }
        return new FetchPlan(targets, rosterSnapshot.available(), rosterSnapshot.selfMarker(), rosterSnapshot.teamOrder());
    }

    private static void sortTargetsByTeamOrder(List<FetchTarget> targets, List<BedwarsTeamMarker> teamOrder) {
        targets.sort(Comparator
                .comparingInt((FetchTarget target) -> getTeamSortIndex(target, teamOrder))
                .thenComparingInt(FetchTarget::listedIndex));
    }

    private static int getTeamSortIndex(FetchTarget target, List<BedwarsTeamMarker> teamOrder) {
        if (target.marker().isEmpty()) {
            return teamOrder.size();
        }

        int index = teamOrder.indexOf(target.marker().get());
        return index >= 0 ? index : teamOrder.size();
    }

    private static boolean shouldFetchTarget(
            FetchTarget target,
            BedwarsRosterResolver.Snapshot rosterSnapshot,
            ModConfig.StatsFetcherConfig config
    ) {
        if (!Boolean.TRUE.equals(config.skipOwnTeamInGame) || !rosterSnapshot.available() || rosterSnapshot.selfMarker().isEmpty()) {
            return true;
        }

        return target.marker().map(marker -> !marker.equals(rosterSnapshot.selfMarker().get())).orElse(true);
    }

    private static void runFetchPlan(Consumer<Component> feedback, FetchPlan fetchPlan) {
        CompletableFuture.supplyAsync(() -> processPlayers(feedback, fetchPlan), AsyncExecutor.getExecutor())
                .thenAcceptAsync(result -> displaySummary(feedback, result), Minecraft.getInstance())
                .exceptionally(ex -> handleError(feedback, ex));
    }

    private static FetchResult processPlayers(Consumer<Component> feedback, FetchPlan fetchPlan) {
        long startNano = System.nanoTime();
        List<PlayerDangerEntry> dangerous = new ArrayList<>();
        int total = fetchPlan.targets().size();

        for (int i = 0; i < total; i++) {
            FetchTarget target = fetchPlan.targets().get(i);
            int current = i + 1;

            try {
                RATE_LIMITER.consume();
                processSinglePlayer(feedback, target, current, total, dangerous);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception exception) {
                handleFetchException(feedback, target, current, total, dangerous, exception);
            }
        }

        double elapsedSeconds = (System.nanoTime() - startNano) / 1_000_000_000.0;
        return new FetchResult(elapsedSeconds, dangerous, fetchPlan.rosterAvailable(), fetchPlan.selfMarker(), fetchPlan.teamOrder());
    }

    private static void processSinglePlayer(Consumer<Component> feedback, FetchTarget target, int current, int total, List<PlayerDangerEntry> dangerous) throws Exception {
        String name = target.name();
        BWStatsExtractor.BWStats stats = ServiceContainer.getStatsExtractor().extractBWStats(name);
        addDangerousPlayerIfNeeded(target, stats, dangerous);

        Minecraft.getInstance().execute(() -> {
            Component header = createPlayerHeader(target, current, total);
            if (Boolean.TRUE.equals(ModConfig.getInstance().statsFetcher.copyButtonsEnabled)) {
                header = StatsCopyComponents.appendCopyButton(header, buildCopyText(name, current, total, stats));
            }
            feedback.accept(header);

            for (Component line : BWStatsFormatter.formatStats(stats)) {
                feedback.accept(line);
            }
        });
    }

    private static Component createPlayerHeader(FetchTarget target, int current, int total) {
        return createPlayerNameComponent(target, false)
                .append(Component.literal("[" + current + "/" + total + "]:").withStyle(ChatFormatting.GOLD));
    }

    private static MutableComponent createPlayerNameComponent(FetchTarget target, boolean nickedStyle) {
        ChatFormatting nameColor = target.marker().map(BedwarsTeamMarker::color).orElse(ChatFormatting.YELLOW);
        return Component.empty()
                .append(Component.literal(target.name()).withStyle(style -> {
                    style = style.withColor(nameColor);
                    if (nickedStyle && target.marker().isPresent()) {
                        style = style.withItalic(true).withUnderlined(true);
                    }
                    return style;
                }))
                .append(Component.literal(" ").withStyle(nameColor));
    }

    private static String buildCopyText(String name, int current, int total, BWStatsExtractor.BWStats stats) {
        String statsText = BWStatsFormatter.formatStatsSummaryText(stats);
        String prefix = name + " [" + current + "/" + total + "]:";
        return statsText.isBlank() ? prefix : prefix + " " + statsText;
    }

    private static void addDangerousPlayerIfNeeded(FetchTarget target, BWStatsExtractor.BWStats stats, List<PlayerDangerEntry> dangerous) {
        String kdValue = stats.getFinalKD();
        if (kdValue == null || BWStatsExtractor.isNotFound(kdValue)) {
            return;
        }

        double kdVal = BWStatsFormatter.parseStatAsDouble(kdValue);
        double threshold = ModConfig.getInstance().statsFetcher.dangerousPlayersKDThreshold;
        if (kdVal > threshold) {
            dangerous.add(new PlayerDangerEntry(target.name(), kdValue, target.marker(), false));
        }
    }

    private static void handleFetchException(
            Consumer<Component> feedback,
            FetchTarget target,
            int current,
            int total,
            List<PlayerDangerEntry> dangerous,
            Exception exception
    ) {
        if (isRateLimited(exception)) {
            retryRateLimitedPlayer(feedback, target, current, total, dangerous);
        } else if (isNetworkError(exception)) {
            handleCouldNotFetchPlayer(feedback, target, current, total, dangerous, NETWORK_ERROR_KEY);
        } else {
            handleNickedPlayer(feedback, target, current, total, dangerous);
        }
    }

    private static void retryRateLimitedPlayer(
            Consumer<Component> feedback,
            FetchTarget target,
            int current,
            int total,
            List<PlayerDangerEntry> dangerous
    ) {
        try {
            Thread.sleep(RATE_LIMIT_RETRY_DELAY_MS);
            RATE_LIMITER.consume();
            processSinglePlayer(feedback, target, current, total, dangerous);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (Exception retryException) {
            if (isRateLimited(retryException)) {
                handleCouldNotFetchPlayer(feedback, target, current, total, dangerous, RATE_LIMIT_KEY);
            } else if (isNetworkError(retryException)) {
                handleCouldNotFetchPlayer(feedback, target, current, total, dangerous, NETWORK_ERROR_KEY);
            } else {
                handleNickedPlayer(feedback, target, current, total, dangerous);
            }
        }
    }

    private static boolean isRateLimited(Throwable exception) {
        return exception instanceof HttpStatusException httpStatusException && httpStatusException.getStatusCode() == 429;
    }

    private static boolean isNetworkError(Throwable exception) {
        return exception instanceof IOException && !(exception instanceof HttpStatusException);
    }

    private static void handleNickedPlayer(Consumer<Component> feedback, FetchTarget target, int current, int total, List<PlayerDangerEntry> dangerous) {
        dangerous.add(new PlayerDangerEntry(target.name(), "nicked", target.marker(), false));
        handlePlayerStatusMessage(feedback, target, current, total, NICKED_KEY, true, "nicked");
    }

    private static void handleCouldNotFetchPlayer(
            Consumer<Component> feedback,
            FetchTarget target,
            int current,
            int total,
            List<PlayerDangerEntry> dangerous,
            String messageKey
    ) {
        dangerous.add(new PlayerDangerEntry(target.name(), COULD_NOT_FETCH_KEY, target.marker(), true));
        handlePlayerStatusMessage(feedback, target, current, total, messageKey, false, getCopyText(COULD_NOT_FETCH_KEY, "couldn't fetch"));
    }

    private static void handlePlayerStatusMessage(Consumer<Component> feedback, FetchTarget target, int current, int total, String messageKey) {
        handlePlayerStatusMessage(feedback, target, current, total, messageKey, false, null);
    }

    private static void handlePlayerStatusMessage(
            Consumer<Component> feedback,
            FetchTarget target,
            int current,
            int total,
            String messageKey,
            boolean nickedStyle,
            String copyStatusText
    ) {
        Minecraft.getInstance().execute(() -> {
            Component header = createPlayerNameComponent(target, nickedStyle)
                    .append(Component.literal("[" + current + "/" + total + "]:").withStyle(ChatFormatting.GOLD));
            if (copyStatusText != null && Boolean.TRUE.equals(ModConfig.getInstance().statsFetcher.copyButtonsEnabled)) {
                header = StatsCopyComponents.appendCopyButton(header, buildStatusCopyText(target.name(), current, total, copyStatusText));
            }
            feedback.accept(header);
            feedback.accept(Component.translatable(messageKey).withStyle(ChatFormatting.RED));
        });
    }

    private static String buildStatusCopyText(String name, int current, int total, String statusText) {
        return name + " [" + current + "/" + total + "]: " + statusText;
    }

    private static void displaySummary(Consumer<Component> feedback, FetchResult result) {
        feedback.accept(
                Component.translatable(SUMMARY_KEY, String.format("%.3f", result.elapsedSeconds()))
                        .withStyle(ChatFormatting.AQUA)
        );

        if (ModConfig.getInstance().statsFetcher.showDangerousPlayers && result.dangerous() != null && !result.dangerous().isEmpty()) {
            if (result.rosterAvailable()) {
                displayGroupedDangerousPlayers(feedback, result);
            } else {
                displayFlatDangerousPlayers(feedback, result.dangerous());
            }
        }
    }

    private static void displayGroupedDangerousPlayers(Consumer<Component> feedback, FetchResult result) {
        feedback.accept(createDangerousTitle(buildGroupedDangerousCopyText(result)));

        Map<BedwarsTeamMarker, List<PlayerDangerEntry>> grouped = groupDangerousPlayers(result.dangerous(), getDisplayedTeamOrder(result));
        for (Map.Entry<BedwarsTeamMarker, List<PlayerDangerEntry>> group : grouped.entrySet()) {
            BedwarsTeamMarker marker = group.getKey();
            List<PlayerDangerEntry> entries = group.getValue();
            Component header = createTeamDangerHeader(marker, entries.size());
            if (!entries.isEmpty() && Boolean.TRUE.equals(ModConfig.getInstance().statsFetcher.copyButtonsEnabled)) {
                header = StatsCopyComponents.appendCopyButton(header, buildTeamDangerousCopyText(marker, entries));
            }
            feedback.accept(header);

            if (entries.isEmpty()) {
                feedback.accept(Component.translatable(NO_DANGEROUS_PLAYER_KEY).withStyle(marker.color()));
                continue;
            }

            List<PlayerDangerEntry> sortedEntries = sortDangerousEntries(entries);
            for (int i = 0; i < sortedEntries.size(); i++) {
                feedback.accept(createDangerEntryComponent(sortedEntries.get(i), i + 1, marker.color()));
            }
        }
    }

    private static void displayFlatDangerousPlayers(Consumer<Component> feedback, List<PlayerDangerEntry> dangerous) {
        feedback.accept(createDangerousTitle(buildFlatDangerousCopyText(dangerous)));
        List<PlayerDangerEntry> sortedEntries = sortDangerousEntries(dangerous);
        for (int i = 0; i < sortedEntries.size(); i++) {
            feedback.accept(createDangerEntryComponent(sortedEntries.get(i), i + 1, ChatFormatting.RED));
        }
    }

    private static Component createDangerousTitle(String copyText) {
        Component title = Component.translatable(DANGEROUS_PLAYERS_KEY).withStyle(ChatFormatting.WHITE);
        if (Boolean.TRUE.equals(ModConfig.getInstance().statsFetcher.copyButtonsEnabled)) {
            return StatsCopyComponents.appendCopyButton(title, copyText);
        }
        return title;
    }

    private static Map<BedwarsTeamMarker, List<PlayerDangerEntry>> groupDangerousPlayers(List<PlayerDangerEntry> dangerous, List<BedwarsTeamMarker> teamOrder) {
        Map<BedwarsTeamMarker, List<PlayerDangerEntry>> grouped = new LinkedHashMap<>();
        for (BedwarsTeamMarker marker : teamOrder) {
            grouped.put(marker, new ArrayList<>());
        }

        for (PlayerDangerEntry entry : dangerous) {
            entry.marker().ifPresent(marker -> grouped.computeIfAbsent(marker, ignored -> new ArrayList<>()).add(entry));
        }

        return grouped;
    }

    private static List<PlayerDangerEntry> sortDangerousEntries(List<PlayerDangerEntry> entries) {
        return entries.stream()
                .sorted(Comparator
                        .comparingInt(WeballCommand::getDangerSortGroup)
                        .thenComparing(Comparator.comparingDouble(WeballCommand::getSortableFkdr).reversed())
                        .thenComparing(PlayerDangerEntry::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static int getDangerSortGroup(PlayerDangerEntry entry) {
        if (entry.couldNotFetch()) {
            return 2;
        }

        if (isNickedDangerEntry(entry)) {
            return 1;
        }

        return 0;
    }

    private static double getSortableFkdr(PlayerDangerEntry entry) {
        double value = BWStatsFormatter.parseStatAsDouble(entry.kd());
        return Double.isNaN(value) ? Double.NEGATIVE_INFINITY : value;
    }

    private static boolean isNickedDangerEntry(PlayerDangerEntry entry) {
        return "nicked".equals(entry.kd());
    }

    private static Component createTeamDangerHeader(BedwarsTeamMarker marker, int count) {
        return Component.literal("[" + getTeamDisplayName(marker) + "](")
                .withStyle(marker.color())
                .append(Component.literal(String.valueOf(count)).withStyle(style -> style
                        .withColor(ChatFormatting.GOLD)
                        .withUnderlined(true)))
                .append(Component.literal("):").withStyle(marker.color()));
    }

    private static Component createDangerEntryComponent(PlayerDangerEntry entry, int index, ChatFormatting fallbackNameColor) {
        ChatFormatting nameColor = entry.marker().map(BedwarsTeamMarker::color).orElse(fallbackNameColor);
        MutableComponent component = Component.literal("");
        component.append(Component.literal(index + ". ").withStyle(ChatFormatting.WHITE));
        component.append(createDangerNameComponent(entry, nameColor));
        component.append(Component.literal(" (").withStyle(ChatFormatting.WHITE));
        component.append(createDangerReasonComponent(entry).withStyle(ChatFormatting.RED));
        component.append(Component.literal(")").withStyle(ChatFormatting.WHITE));
        return component;
    }

    private static MutableComponent createDangerReasonComponent(PlayerDangerEntry entry) {
        if (entry.couldNotFetch()) {
            return Component.translatable(entry.kd());
        }

        return Component.literal(entry.kd());
    }

    private static MutableComponent createDangerNameComponent(PlayerDangerEntry entry, ChatFormatting nameColor) {
        if (entry.couldNotFetch()) {
            return createCouldNotFetchNameComponent(entry, nameColor);
        }

        MutableComponent component = Component.empty();
        component.append(Component.literal(entry.name()).withStyle(style -> {
                    style = style.withColor(nameColor);
                    if (isNickedDangerEntry(entry) && entry.marker().isPresent()) {
                        style = style.withItalic(true).withUnderlined(true);
                    }
                    return style;
                }));
        return component;
    }

    private static MutableComponent createCouldNotFetchNameComponent(PlayerDangerEntry entry, ChatFormatting nameColor) {
        MutableComponent component = Component.literal("");
        component.append(Component.literal("x").withStyle(nameColor, ChatFormatting.OBFUSCATED));
        component.append(Component.literal(entry.name()).withStyle(style -> {
            style = style.withColor(nameColor).withObfuscated(false).withItalic(false);
            if (entry.marker().isPresent()) {
                style = style.withUnderlined(true);
            }
            return style;
        }));
        component.append(Component.literal("x").withStyle(nameColor, ChatFormatting.OBFUSCATED));
        return component;
    }

    private static String buildGroupedDangerousCopyText(FetchResult result) {
        Map<BedwarsTeamMarker, List<PlayerDangerEntry>> grouped = groupDangerousPlayers(result.dangerous(), getDisplayedTeamOrder(result));
        List<String> parts = new ArrayList<>();
        for (Map.Entry<BedwarsTeamMarker, List<PlayerDangerEntry>> group : grouped.entrySet()) {
            parts.add(buildTeamDangerousCopyText(group.getKey(), group.getValue()));
        }
        return getDangerousPlayersCopyPrefix() + " " + String.join(" | ", parts);
    }

    private static List<BedwarsTeamMarker> getDisplayedTeamOrder(FetchResult result) {
        if (!Boolean.TRUE.equals(ModConfig.getInstance().statsFetcher.skipOwnTeamInGame) || result.selfMarker().isEmpty()) {
            return result.teamOrder();
        }

        return result.teamOrder().stream()
                .filter(marker -> !marker.equals(result.selfMarker().get()))
                .toList();
    }

    private static String buildTeamDangerousCopyText(BedwarsTeamMarker marker, List<PlayerDangerEntry> entries) {
        String prefix = "[" + getTeamDisplayName(marker) + "](" + entries.size() + "):";
        if (entries.isEmpty()) {
            return prefix + " " + getCopyText(NO_DANGEROUS_PLAYER_KEY, "No dangerous player.");
        }

        return prefix + " " + sortDangerousEntries(entries).stream()
                .map(entry -> entry.name() + " (" + getDangerCopyReason(entry) + ")")
                .collect(Collectors.joining(" | "));
    }

    private static String buildFlatDangerousCopyText(List<PlayerDangerEntry> dangerous) {
        return getDangerousPlayersCopyPrefix() + " " + sortDangerousEntries(dangerous).stream()
                .map(entry -> entry.name() + " (" + getDangerCopyReason(entry) + ")")
                .collect(Collectors.joining(" | "));
    }

    private static String getDangerousPlayersCopyPrefix() {
        return getCopyText(DANGEROUS_PLAYERS_KEY, "Dangerous Players:");
    }

    private static String getDangerCopyReason(PlayerDangerEntry entry) {
        if (entry.couldNotFetch()) {
            return getCopyText(entry.kd(), "couldn't fetch");
        }

        return entry.kd();
    }

    private static String getCopyText(String key, String englishText) {
        if (Boolean.TRUE.equals(ModConfig.getInstance().statsFetcher.copyTextInEnglish)) {
            return englishText;
        }

        return Component.translatable(key).getString();
    }

    private static String getTeamDisplayName(BedwarsTeamMarker marker) {
        if (marker.equals(BedwarsTeamMarker.RED)) {
            return "Red";
        } else if (marker.equals(BedwarsTeamMarker.BLUE)) {
            return "Blue";
        } else if (marker.equals(BedwarsTeamMarker.GREEN)) {
            return "Green";
        } else if (marker.equals(BedwarsTeamMarker.YELLOW)) {
            return "Yellow";
        } else if (marker.equals(BedwarsTeamMarker.AQUA)) {
            return "Aqua";
        } else if (marker.equals(BedwarsTeamMarker.WHITE)) {
            return "White";
        } else if (marker.equals(BedwarsTeamMarker.PINK)) {
            return "Pink";
        } else if (marker.equals(BedwarsTeamMarker.GRAY)) {
            return "Gray";
        }

        return marker.letter();
    }

    private static Void handleError(Consumer<Component> feedback, Throwable exception) {
        LOGGER.error("Weball execution failed", exception);
        Minecraft.getInstance().execute(() -> feedback.accept(
                Component.translatable(FAILED_KEY, exception.getMessage()).withStyle(ChatFormatting.RED)
        ));
        return null;
    }

    private record FetchPlan(
            List<FetchTarget> targets,
            boolean rosterAvailable,
            Optional<BedwarsTeamMarker> selfMarker,
            List<BedwarsTeamMarker> teamOrder
    ) {
    }

    private record FetchTarget(String name, Optional<BedwarsTeamMarker> marker, int listedIndex) {
    }

    private record FetchResult(
            double elapsedSeconds,
            List<PlayerDangerEntry> dangerous,
            boolean rosterAvailable,
            Optional<BedwarsTeamMarker> selfMarker,
            List<BedwarsTeamMarker> teamOrder
    ) {
    }

    private record PlayerDangerEntry(String name, String kd, Optional<BedwarsTeamMarker> marker, boolean couldNotFetch) {
    }
}
