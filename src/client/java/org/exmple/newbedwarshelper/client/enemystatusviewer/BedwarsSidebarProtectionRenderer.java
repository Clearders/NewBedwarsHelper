package org.exmple.newbedwarshelper.client.enemystatusviewer;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.exmple.newbedwarshelper.client.enemystatusviewer.BedwarsProtectionTracker.ArmorUpgradeStatus;
import org.exmple.newbedwarshelper.client.utils.bedwars.BedwarsGameDetector;
import org.exmple.newbedwarshelper.client.utils.bedwars.BedwarsSidebarTeamParser;
import org.exmple.newbedwarshelper.client.utils.bedwars.BedwarsTeamMarker;

public final class BedwarsSidebarProtectionRenderer {
    private static boolean renderingSidebar;
    private static Optional<BedwarsTeamMarker> currentSelfTeamMarker = Optional.empty();

    private BedwarsSidebarProtectionRenderer() {
    }

    public static void beginSidebarRender() {
        currentSelfTeamMarker = BedwarsGameDetector.isInGame()
                ? BedwarsGameDetector.getCurrentSelfTeamMarker(Minecraft.getInstance())
                : Optional.empty();
        renderingSidebar = true;
    }

    public static void endSidebarRender() {
        renderingSidebar = false;
        currentSelfTeamMarker = Optional.empty();
    }

    public static MutableComponent appendProtectionLevel(Team team, MutableComponent displayedName) {
        if (!renderingSidebar) {
            return displayedName;
        }

        if (!BedwarsGameDetector.isInGame()) {
            return displayedName;
        }

        if (!(team instanceof PlayerTeam playerTeam)) {
            return displayedName;
        }

        return BedwarsSidebarTeamParser.identify(playerTeam, displayedName).map(marker -> {
            ArmorUpgradeStatus status = BedwarsProtectionTracker.getArmorStatus(marker).orElse(null);
            if (status == null) {
                return displayedName;
            }

            return appendArmorStatus(displayedName, marker, status);
        }).orElse(displayedName);
    }

    private static MutableComponent appendArmorStatus(
            MutableComponent displayedName,
            BedwarsTeamMarker marker,
            ArmorUpgradeStatus status
    ) {
        return currentSelfTeamMarker
                .map(selfMarker -> marker.equals(selfMarker)
                        ? appendSelfTeamArmorStatus(displayedName, status)
                        : appendEnemyArmorStatus(displayedName, status))
                .orElse(displayedName);
    }

    private static MutableComponent appendSelfTeamArmorStatus(MutableComponent displayedName, ArmorUpgradeStatus status) {
        int protectionLevel = status.protectionLevel();
        int featherFallingLevel = status.featherFallingLevel();
        if (protectionLevel <= 0 && featherFallingLevel <= 0) {
            return displayedName;
        }

        if (protectionLevel > 0 && featherFallingLevel <= 0) {
            return displayedName.append(Component.literal(" [P" + protectionLevel + "]").withStyle(getLevelColor(protectionLevel)));
        }

        if (protectionLevel <= 0) {
            return displayedName.append(Component.literal(" [FF" + featherFallingLevel + "]").withStyle(getFeatherFallingColor(featherFallingLevel)));
        }

        MutableComponent suffix = Component.literal(" [");
        suffix.append(Component.literal("P" + protectionLevel).withStyle(getLevelColor(protectionLevel)));
        suffix.append(Component.literal(","));
        suffix.append(Component.literal("FF" + featherFallingLevel).withStyle(getFeatherFallingColor(featherFallingLevel)));
        suffix.append(Component.literal("]"));
        return displayedName.append(suffix);
    }

    private static MutableComponent appendEnemyArmorStatus(MutableComponent displayedName, ArmorUpgradeStatus status) {
        if (!status.enemyFullArmorGlint()) {
            return displayedName;
        }

        return displayedName.append(Component.literal(" [P:\u2713]").withStyle(getLevelColor(1)));
    }

    private static ChatFormatting getLevelColor(int level) {
        return switch (level) {
            case 1 -> ChatFormatting.BLUE;
            case 2 -> ChatFormatting.YELLOW;
            case 3 -> ChatFormatting.GOLD;
            default -> ChatFormatting.RED;
        };
    }

    private static ChatFormatting getFeatherFallingColor(int level) {
        return switch (level) {
            case 1 -> ChatFormatting.GREEN;
            default -> ChatFormatting.GOLD;
        };
    }
}
