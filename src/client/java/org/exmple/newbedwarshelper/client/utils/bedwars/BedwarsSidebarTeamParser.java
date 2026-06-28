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

    public static Optional<BedwarsTeamMarker> identifySelfTeam(PlayerTeam team, Component displayedName) {
        return identify(team, displayedName).filter(ignored -> hasTrailingSelfMarker(displayedName.getString()));
    }

    public static boolean containsKnownSelfMarkerText(Component displayedName) {
        String text = displayedName.getString();
        return text != null && (text.toUpperCase(Locale.ROOT).contains("YOU") || text.contains("\u4F60"));
    }

    private static boolean containsColoredTeamLetter(String text, BedwarsTeamMarker marker) {
        if (text == null) {
            return false;
        }

        return text.toUpperCase(Locale.ROOT).contains((marker.color().toString() + marker.letter()).toUpperCase(Locale.ROOT));
    }

    private static boolean hasTrailingSelfMarker(String text) {
        if (text == null) {
            return false;
        }

        String cleanText = text.replaceAll("\u00A7.", "").trim();
        int separatorIndex = cleanText.lastIndexOf(':');
        if (separatorIndex < 0 || separatorIndex + 1 >= cleanText.length()) {
            return false;
        }

        String statusAndSuffix = cleanText.substring(separatorIndex + 1).trim();
        return statusAndSuffix.matches("^(?:\\d+|[\\u2713\\u2714\\u2717\\u2718xX])\\s+\\S.*$");
    }
}
