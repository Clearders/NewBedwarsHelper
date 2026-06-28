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
        ClientTickEvents.END_CLIENT_TICK.register(BedwarsGameDetector::onClientTick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> endDetectedGame(client));
    }

    public static boolean isInGame() {
        return inDetectedGame;
    }

    public static Optional<BedwarsTeamMarker> getCurrentSelfTeamMarker(Minecraft client) {
        if (!isInGame()) {
            return Optional.empty();
        }

        if (client.level == null || client.player == null) {
            return Optional.empty();
        }

        Scoreboard scoreboard = client.level.getScoreboard();
        Objective sidebar = getVisibleSidebarObjective(client, scoreboard);
        if (sidebar == null) {
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
            Optional<BedwarsTeamMarker> marker = BedwarsSidebarTeamParser.identifySelfTeam(team, displayedName);
            if (marker.isPresent()) {
                return marker;
            }
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
            if (BedwarsSidebarTeamParser.containsKnownSelfMarkerText(displayedName)) {
                return BedwarsSidebarTeamParser.identify(team, displayedName);
            }
        }

        return getSelfMarkerFromPlayerTeam(client, scoreboard);
    }

    private static void onClientTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            endDetectedGame(client);
            return;
        }

        if (inDetectedGame) {
            if (client.level != detectedLevel || client.level.dimension() != detectedDimension) {
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
        if (hasLayout) {
            startDetectedGame(client);
        }
    }

    private static void startDetectedGame(Minecraft client) {
        inDetectedGame = true;
        detectedLevel = client.level;
        detectedDimension = client.level.dimension();
    }

    private static boolean hasBedwarsSidebarTeamLayout(Minecraft client) {
        Set<BedwarsTeamMarker> markers = getVisibleSidebarTeamMarkers(client);
        boolean normal = markers.containsAll(BedwarsTeamMarker.NORMAL_MODE_REQUIRED);
        boolean twoTeam = markers.containsAll(BedwarsTeamMarker.TWO_TEAM_MODE_REQUIRED)
                && !markers.contains(BedwarsTeamMarker.GREEN)
                && !markers.contains(BedwarsTeamMarker.YELLOW)
                && BedwarsTeamMarker.EXTRA_TEAMS.stream().noneMatch(markers::contains);

        if (normal) {
            return true;
        }

        return twoTeam;
    }

    private static Set<BedwarsTeamMarker> getVisibleSidebarTeamMarkers(Minecraft client) {
        Scoreboard scoreboard = client.level.getScoreboard();
        Objective sidebar = getVisibleSidebarObjective(client, scoreboard);
        if (sidebar == null) {
            return Set.of();
        }

        Set<BedwarsTeamMarker> markers = new HashSet<>();
        for (PlayerScoreEntry score : scoreboard.listPlayerScores(sidebar)) {
            if (score.isHidden()) {
                continue;
            }

            PlayerTeam team = scoreboard.getPlayersTeam(score.owner());
            if (team == null) {
                continue;
            }

            Component displayedName = PlayerTeam.formatNameForTeam(team, score.ownerName());
            BedwarsSidebarTeamParser.identify(team, displayedName).ifPresent(markers::add);
        }

        return markers;
    }

    private static Objective getVisibleSidebarObjective(Minecraft client, Scoreboard scoreboard) {
        Objective teamObjective = null;
        PlayerTeam playerTeam = scoreboard.getPlayersTeam(client.player.getScoreboardName());
        if (playerTeam != null) {
            DisplaySlot displaySlot = playerTeam.getColor()
                    .map(net.minecraft.world.scores.TeamColor::displaySlot)
                    .orElse(null);
            if (displaySlot != null) {
                teamObjective = scoreboard.getDisplayObjective(displaySlot);
            }
        }

        return teamObjective != null ? teamObjective : scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
    }

    private static Optional<BedwarsTeamMarker> getSelfMarkerFromPlayerTeam(Minecraft client, Scoreboard scoreboard) {
        PlayerTeam playerTeam = scoreboard.getPlayersTeam(client.player.getScoreboardName());
        if (playerTeam == null) {
            return Optional.empty();
        }

        return BedwarsTeamMarker.fromColor(playerTeam.getColor());
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
        BedwarsProtectionTracker.clear();
        BedwarsInvisibilityToastNotifier.clear();
    }
}
