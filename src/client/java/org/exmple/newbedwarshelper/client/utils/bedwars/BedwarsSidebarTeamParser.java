package org.exmple.newbedwarshelper.client.utils.bedwars;

import java.util.Locale;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;

public final class BedwarsSidebarTeamParser {
    private BedwarsSidebarTeamParser() {
    }

    public static Optional<BedwarsTeamMarker> identify(PlayerTeam team, Component displayedName) {
        String rawText = displayedName.getString();
        for (BedwarsTeamMarker marker : BedwarsTeamMarker.ALL) {
            if (containsColoredTeamLetter(rawText, marker)) {
                return Optional.of(marker);
            }
        }

        return Optional.empty();
    }

    private static boolean containsColoredTeamLetter(String text, BedwarsTeamMarker marker) {
        if (text == null) {
            return false;
        }

        return text.toUpperCase(Locale.ROOT).contains((marker.color().toString() + marker.letter()).toUpperCase(Locale.ROOT));
    }

}
