package org.exmple.newbedwarshelper.client.utils.bedwars;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.PlayerTeam;
import org.exmple.newbedwarshelper.client.utils.ServiceContainer;

public final class BedwarsRosterResolver {
    private static final List<BedwarsTeamMarker> TWO_TEAM_ORDER = List.of(
            BedwarsTeamMarker.RED,
            BedwarsTeamMarker.BLUE
    );
    private static final List<BedwarsTeamMarker> FOUR_TEAM_ORDER = List.of(
            BedwarsTeamMarker.RED,
            BedwarsTeamMarker.BLUE,
            BedwarsTeamMarker.GREEN,
            BedwarsTeamMarker.YELLOW
    );
    private static final List<BedwarsTeamMarker> EIGHT_TEAM_ORDER = List.of(
            BedwarsTeamMarker.RED,
            BedwarsTeamMarker.BLUE,
            BedwarsTeamMarker.GREEN,
            BedwarsTeamMarker.YELLOW,
            BedwarsTeamMarker.AQUA,
            BedwarsTeamMarker.WHITE,
            BedwarsTeamMarker.PINK,
            BedwarsTeamMarker.GRAY
    );

    private BedwarsRosterResolver() {
    }

    public static Snapshot resolve(Minecraft client) {
        if (!BedwarsGameDetector.isInGame() || client.level == null || client.player == null) {
            return Snapshot.unavailable();
        }

        Optional<BedwarsTeamMarker> selfMarker = BedwarsGameDetector.getCurrentSelfTeamMarker(client);
        if (selfMarker.isEmpty()) {
            return Snapshot.unavailable();
        }

        ClientPacketListener connection = client.getConnection();
        if (connection == null) {
            return Snapshot.unavailable();
        }

        List<String> playerOrder = new ArrayList<>();
        Map<String, BedwarsTeamMarker> playerTeams = new LinkedHashMap<>();
        for (PlayerInfo info : connection.getListedOnlinePlayers()) {
            String name = ServiceContainer.getNameFormatter().cleanPlayerName(info.getProfile().name());
            playerOrder.add(name);
            PlayerTeam team = client.level.getScoreboard().getPlayersTeam(info.getProfile().name());
            if (team != null) {
                BedwarsTeamMarker.fromColor(team.getColor()).ifPresent(marker -> playerTeams.put(name, marker));
            }
        }

        if (playerOrder.isEmpty()) {
            return Snapshot.unavailable();
        }

        return new Snapshot(true, selfMarker, determineTeamOrder(playerTeams), playerOrder, playerTeams);
    }

    private static List<BedwarsTeamMarker> determineTeamOrder(Map<String, BedwarsTeamMarker> playerTeams) {
        long presentTeamCount = playerTeams.values().stream().distinct().count();
        if (presentTeamCount <= 2 && playerTeams.values().stream().allMatch(TWO_TEAM_ORDER::contains)) {
            return TWO_TEAM_ORDER;
        }

        if (presentTeamCount <= 4 && playerTeams.values().stream().allMatch(FOUR_TEAM_ORDER::contains)) {
            return FOUR_TEAM_ORDER;
        }

        return EIGHT_TEAM_ORDER;
    }

    public record Snapshot(
            boolean available,
            Optional<BedwarsTeamMarker> selfMarker,
            List<BedwarsTeamMarker> teamOrder,
            List<String> playerOrder,
            Map<String, BedwarsTeamMarker> playerTeams
    ) {
        private static Snapshot unavailable() {
            return new Snapshot(false, Optional.empty(), List.of(), List.of(), Map.of());
        }

        public Optional<BedwarsTeamMarker> teamForPlayer(String playerName) {
            return Optional.ofNullable(playerTeams.get(playerName));
        }
    }
}
