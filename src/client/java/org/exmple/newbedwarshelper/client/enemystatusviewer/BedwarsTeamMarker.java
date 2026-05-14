package org.exmple.newbedwarshelper.client.enemystatusviewer;

import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;

public record BedwarsTeamMarker(ChatFormatting color, String letter) {
    public static final BedwarsTeamMarker RED = new BedwarsTeamMarker(ChatFormatting.RED, "R");
    public static final BedwarsTeamMarker BLUE = new BedwarsTeamMarker(ChatFormatting.BLUE, "B");
    public static final BedwarsTeamMarker GREEN = new BedwarsTeamMarker(ChatFormatting.GREEN, "G");
    public static final BedwarsTeamMarker YELLOW = new BedwarsTeamMarker(ChatFormatting.YELLOW, "Y");
    public static final BedwarsTeamMarker AQUA = new BedwarsTeamMarker(ChatFormatting.AQUA, "A");
    public static final BedwarsTeamMarker WHITE = new BedwarsTeamMarker(ChatFormatting.WHITE, "W");
    public static final BedwarsTeamMarker PINK = new BedwarsTeamMarker(ChatFormatting.LIGHT_PURPLE, "P");
    public static final BedwarsTeamMarker GRAY = new BedwarsTeamMarker(ChatFormatting.GRAY, "S");

    public static final Set<BedwarsTeamMarker> NORMAL_MODE_REQUIRED = Set.of(RED, BLUE, GREEN, YELLOW);
    public static final Set<BedwarsTeamMarker> TWO_TEAM_MODE_REQUIRED = Set.of(RED, BLUE);
    public static final Set<BedwarsTeamMarker> EXTRA_TEAMS = Set.of(AQUA, WHITE, PINK, GRAY);
    public static final Set<BedwarsTeamMarker> ALL = Set.of(RED, BLUE, GREEN, YELLOW, AQUA, WHITE, PINK, GRAY);

    public static Optional<BedwarsTeamMarker> fromColor(ChatFormatting color) {
        for (BedwarsTeamMarker marker : ALL) {
            if (marker.color == color) {
                return Optional.of(marker);
            }
        }

        return Optional.empty();
    }

    public String debugName() {
        return letter + "/" + color.getName();
    }
}
