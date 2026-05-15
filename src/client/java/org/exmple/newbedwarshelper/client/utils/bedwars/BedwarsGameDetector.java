package org.exmple.newbedwarshelper.client.utils.bedwars;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.exmple.newbedwarshelper.client.enemystatusviewer.BedwarsDebugLogger;
import org.exmple.newbedwarshelper.client.enemystatusviewer.BedwarsInvisibilityToastNotifier;
import org.exmple.newbedwarshelper.client.enemystatusviewer.BedwarsProtectionTracker;

public final class BedwarsGameDetector {
    private static final int CHECK_INTERVAL_TICKS = 20;

    private static boolean inDetectedGame;
    private static int ticksUntilNextCheck;
    private static ClientLevel detectedLevel;
    private static ResourceKey<Level> detectedDimension;

    private BedwarsGameDetector() {
    }

    public static void init() {
        BedwarsDebugLogger.detector("init registered");
        ClientTickEvents.END_CLIENT_TICK.register(BedwarsGameDetector::onClientTick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> endDetectedGame(client));
    }

    public static boolean isInGame() {
        return inDetectedGame;
    }

    public static Optional<BedwarsTeamMarker> getCurrentSelfTeamMarker(Minecraft client) {
        if (client.level == null || client.player == null) {
            BedwarsDebugLogger.detector("self marker unavailable: level or player is null");
            return Optional.empty();
        }

        Scoreboard scoreboard = client.level.getScoreboard();
        Objective sidebar = getVisibleSidebarObjective(client, scoreboard);
        if (sidebar == null) {
            BedwarsDebugLogger.detector("self marker unavailable: sidebar objective is null");
            return Optional.empty();
        }

        for (PlayerScoreEntry score : scoreboard.listPlayerScores(sidebar)) {
            if (score.isHidden()) {
                continue;
            }

            PlayerTeam team = scoreboard.getPlayersTeam(score.owner());
            if (team == null) {
                continue;
            }

            Component displayedName = PlayerTeam.formatNameForTeam(team, score.ownerName());
            if (!displayedName.getString().contains("YOU")) {
                continue;
            }

            Optional<BedwarsTeamMarker> marker = BedwarsSidebarTeamParser.identify(team, displayedName);
            BedwarsDebugLogger.detector("self marker row found: text='"
                    + displayedName.getString()
                    + "', marker=" + marker.map(BedwarsTeamMarker::debugName).orElse("none"));
            return marker;
        }

        BedwarsDebugLogger.detector("self marker unavailable: no sidebar row contains YOU");
        return Optional.empty();
    }

    private static void onClientTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            BedwarsDebugLogger.detector("skip tick: level or player is null");
            endDetectedGame(client);
            return;
        }

        if (inDetectedGame) {
            if (client.level != detectedLevel || client.level.dimension() != detectedDimension) {
                BedwarsDebugLogger.detector("ending game: level or dimension changed");
                endDetectedGame(client);
            }
            return;
        }

        if (ticksUntilNextCheck > 0) {
            ticksUntilNextCheck--;
            return;
        }
        ticksUntilNextCheck = CHECK_INTERVAL_TICKS;

        boolean hasLayout = hasBedwarsSidebarTeamLayout(client);
        BedwarsDebugLogger.detector("layout check result=" + hasLayout);
        if (hasLayout) {
            startDetectedGame(client);
        }
    }

    private static void startDetectedGame(Minecraft client) {
        inDetectedGame = true;
        detectedLevel = client.level;
        detectedDimension = client.level.dimension();
        BedwarsDebugLogger.detector("started detected game; dimension=" + detectedDimension.identifier());
    }

    private static boolean hasBedwarsSidebarTeamLayout(Minecraft client) {
        Set<BedwarsTeamMarker> markers = getVisibleSidebarTeamMarkers(client);
        boolean normal = markers.containsAll(BedwarsTeamMarker.NORMAL_MODE_REQUIRED);
        boolean twoTeam = markers.containsAll(BedwarsTeamMarker.TWO_TEAM_MODE_REQUIRED)
                && !markers.contains(BedwarsTeamMarker.GREEN)
                && !markers.contains(BedwarsTeamMarker.YELLOW)
                && BedwarsTeamMarker.EXTRA_TEAMS.stream().noneMatch(markers::contains);

        BedwarsDebugLogger.detector("markers=" + describeMarkers(markers) + ", normal=" + normal + ", twoTeam=" + twoTeam);
        if (normal) {
            return true;
        }

        return twoTeam;
    }

    private static Set<BedwarsTeamMarker> getVisibleSidebarTeamMarkers(Minecraft client) {
        Scoreboard scoreboard = client.level.getScoreboard();
        Objective sidebar = getVisibleSidebarObjective(client, scoreboard);
        if (sidebar == null) {
            BedwarsDebugLogger.detector("sidebar objective is null");
            return Set.of();
        }

        BedwarsDebugLogger.detector("sidebar objective name=" + sidebar.getName() + ", display=" + sidebar.getDisplayName().getString());
        Set<BedwarsTeamMarker> markers = new HashSet<>();
        for (PlayerScoreEntry score : scoreboard.listPlayerScores(sidebar)) {
            if (score.isHidden()) {
                BedwarsDebugLogger.detector("sidebar row hidden owner=" + score.owner());
                continue;
            }

            PlayerTeam team = scoreboard.getPlayersTeam(score.owner());
            if (team == null) {
                BedwarsDebugLogger.detector("sidebar row owner=" + score.owner() + ", no team, ownerName=" + score.ownerName().getString());
                continue;
            }

            Component displayedName = PlayerTeam.formatNameForTeam(team, score.ownerName());
            BedwarsSidebarTeamParser.identify(team, displayedName).ifPresentOrElse(marker -> {
                markers.add(marker);
                BedwarsDebugLogger.detector("sidebar row matched owner=" + score.owner()
                        + ", team=" + team.getName()
                        + ", color=" + team.getColor().getName()
                        + ", text='" + displayedName.getString()
                        + "', marker=" + marker.debugName());
            }, () -> BedwarsDebugLogger.detector("sidebar row unmatched owner=" + score.owner()
                    + ", team=" + team.getName()
                    + ", color=" + team.getColor().getName()
                    + ", text='" + displayedName.getString() + "'"));
        }

        return markers;
    }

    private static Objective getVisibleSidebarObjective(Minecraft client, Scoreboard scoreboard) {
        Objective teamObjective = null;
        PlayerTeam playerTeam = scoreboard.getPlayersTeam(client.player.getScoreboardName());
        if (playerTeam != null) {
            DisplaySlot displaySlot = DisplaySlot.teamColorToSlot(playerTeam.getColor());
            BedwarsDebugLogger.detector("self team=" + playerTeam.getName()
                    + ", color=" + playerTeam.getColor().getName()
                    + ", team sidebar slot=" + (displaySlot == null ? "null" : displaySlot.getSerializedName()));
            if (displaySlot != null) {
                teamObjective = scoreboard.getDisplayObjective(displaySlot);
                BedwarsDebugLogger.detector("team sidebar objective=" + (teamObjective == null ? "null" : teamObjective.getName()));
            }
        } else {
            BedwarsDebugLogger.detector("self has no scoreboard team");
        }

        return teamObjective != null ? teamObjective : scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
    }

    private static String describeMarkers(Set<BedwarsTeamMarker> markers) {
        if (markers.isEmpty()) {
            return "[]";
        }

        return markers.stream().map(BedwarsTeamMarker::debugName).sorted().toList().toString();
    }

    private static void endDetectedGame(Minecraft client) {
        if (!inDetectedGame) {
            detectedLevel = null;
            detectedDimension = null;
            return;
        }

        inDetectedGame = false;
        detectedLevel = null;
        detectedDimension = null;
        ticksUntilNextCheck = 0;
        BedwarsDebugLogger.detector("ended detected game; clearing protection cache");
        BedwarsProtectionTracker.clear();
        BedwarsInvisibilityToastNotifier.clear();
    }
}
