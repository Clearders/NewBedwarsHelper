package org.exmple.newbedwarshelper.client.enemystatusviewer;

import java.util.OptionalInt;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

public final class BedwarsSidebarProtectionRenderer {
    private static boolean renderingSidebar;

    private BedwarsSidebarProtectionRenderer() {
    }

    public static void beginSidebarRender() {
        renderingSidebar = true;
        BedwarsDebugLogger.renderer("begin sidebar render");
    }

    public static void endSidebarRender() {
        renderingSidebar = false;
        BedwarsDebugLogger.renderer("end sidebar render");
    }

    public static MutableComponent appendProtectionLevel(Team team, MutableComponent displayedName) {
        if (!renderingSidebar) {
            return displayedName;
        }

        if (!BedwarsGameDetector.isInGame()) {
            BedwarsDebugLogger.renderer("skip append: not in game, text='" + displayedName.getString() + "'");
            return displayedName;
        }

        if (!(team instanceof PlayerTeam playerTeam)) {
            BedwarsDebugLogger.renderer("skip append: team is not PlayerTeam, text='" + displayedName.getString() + "'");
            return displayedName;
        }

        return BedwarsSidebarTeamParser.identify(playerTeam, displayedName).map(marker -> {
            OptionalInt level = BedwarsProtectionTracker.getProtectionLevel(marker);
            if (level.isEmpty()) {
                BedwarsDebugLogger.renderer("matched marker=" + marker.debugName()
                        + ", no cached protection, text='" + displayedName.getString() + "'");
                return displayedName;
            }

            BedwarsDebugLogger.renderer("append marker=" + marker.debugName()
                    + ", level=" + level.getAsInt()
                    + ", text='" + displayedName.getString() + "'");
            return displayedName.append(Component.literal(" [P" + level.getAsInt() + "]").withStyle(getLevelColor(level.getAsInt())));
        }).orElseGet(() -> {
            BedwarsDebugLogger.renderer("skip append: no marker, team=" + playerTeam.getName()
                    + ", color=" + playerTeam.getColor().getName()
                    + ", text='" + displayedName.getString() + "'");
            return displayedName;
        });
    }

    private static ChatFormatting getLevelColor(int level) {
        return switch (level) {
            case 1 -> ChatFormatting.BLUE;
            case 2 -> ChatFormatting.YELLOW;
            case 3 -> ChatFormatting.GOLD;
            default -> ChatFormatting.RED;
        };
    }
}
