package org.exmple.newbedwarshelper.client.enemystatusviewer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.scores.PlayerTeam;

public final class BedwarsProtectionTracker {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };
    private static final int CHECK_INTERVAL_TICKS = 20;

    private static final Map<BedwarsTeamMarker, Integer> teamProtectionLevels = new HashMap<>();
    private static int ticksUntilNextCheck;

    private BedwarsProtectionTracker() {
    }

    public static void init() {
        BedwarsDebugLogger.tracker("init registered");
        ClientTickEvents.END_CLIENT_TICK.register(BedwarsProtectionTracker::onClientTick);
    }

    public static OptionalInt getProtectionLevel(BedwarsTeamMarker marker) {
        Integer level = teamProtectionLevels.get(marker);
        return level == null ? OptionalInt.empty() : OptionalInt.of(level);
    }

    public static void clear() {
        teamProtectionLevels.clear();
        ticksUntilNextCheck = 0;
        BedwarsDebugLogger.tracker("cache cleared");
    }

    private static void onClientTick(Minecraft client) {
        if (ticksUntilNextCheck > 0) {
            ticksUntilNextCheck--;
            return;
        }
        ticksUntilNextCheck = CHECK_INTERVAL_TICKS;

        if (!BedwarsGameDetector.isInGame()) {
            BedwarsDebugLogger.tracker("skip tick: detector says not in game");
            return;
        }

        if (client.level == null) {
            BedwarsDebugLogger.tracker("skip tick: level is null");
            return;
        }

        Holder<Enchantment> protection = client.level.registryAccess().getOrThrow(Enchantments.PROTECTION);
        BedwarsDebugLogger.tracker("scan players count=" + client.level.players().size() + ", cache=" + describeCache());
        for (AbstractClientPlayer player : client.level.players()) {
            Optional<BedwarsTeamMarker> marker = getPlayerTeamMarker(player);
            if (marker.isEmpty()) {
                BedwarsDebugLogger.tracker("player=" + player.getScoreboardName() + ", no BedWars marker, team="
                        + (player.getTeam() == null ? "null" : player.getTeam().getName()));
                continue;
            }

            int level = getMaxProtectionLevel(player, protection);
            BedwarsDebugLogger.tracker("player=" + player.getScoreboardName()
                    + ", marker=" + marker.get().debugName()
                    + ", maxProtection=" + level
                    + ", armor=" + describeArmor(player, protection));
            updateTeamProtection(marker.get(), level);
        }
    }

    private static Optional<BedwarsTeamMarker> getPlayerTeamMarker(AbstractClientPlayer player) {
        if (!(player.getTeam() instanceof PlayerTeam team)) {
            return Optional.empty();
        }

        return BedwarsTeamMarker.fromColor(team.getColor());
    }

    private static int getMaxProtectionLevel(AbstractClientPlayer player, Holder<Enchantment> protection) {
        int maxLevel = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            maxLevel = Math.max(maxLevel, EnchantmentHelper.getItemEnchantmentLevel(protection, stack));
        }

        return maxLevel;
    }

    private static void updateTeamProtection(BedwarsTeamMarker marker, int level) {
        if (level <= 0) {
            BedwarsDebugLogger.tracker("ignore marker=" + marker.debugName() + ", level=" + level);
            return;
        }

        Integer oldLevel = teamProtectionLevels.get(marker);
        teamProtectionLevels.merge(marker, level, Math::max);
        Integer newLevel = teamProtectionLevels.get(marker);
        BedwarsDebugLogger.tracker("update marker=" + marker.debugName()
                + ", old=" + (oldLevel == null ? "none" : oldLevel)
                + ", observed=" + level
                + ", new=" + newLevel);
    }

    private static String describeArmor(AbstractClientPlayer player, Holder<Enchantment> protection) {
        StringBuilder builder = new StringBuilder();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!builder.isEmpty()) {
                builder.append(", ");
            }

            ItemStack stack = player.getItemBySlot(slot);
            builder.append(slot.getName())
                    .append("=")
                    .append(describeArmorStack(stack, protection));
        }

        return builder.toString();
    }

    private static String describeArmorStack(ItemStack stack, Holder<Enchantment> protection) {
        if (stack.isEmpty()) {
            return "empty";
        }

        ItemEnchantments enchantments = stack.getEnchantments();
        Boolean glintOverride = stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        return stack.getHoverName().getString()
                + "{prot=" + EnchantmentHelper.getItemEnchantmentLevel(protection, stack)
                + ", enchanted=" + stack.isEnchanted()
                + ", foil=" + stack.hasFoil()
                + ", glintOverride=" + (glintOverride == null ? "none" : glintOverride)
                + ", enchantCount=" + enchantments.size()
                + ", enchants=" + describeEnchantments(enchantments)
                + "}";
    }

    private static String describeEnchantments(ItemEnchantments enchantments) {
        if (enchantments.isEmpty()) {
            return "[]";
        }

        return enchantments.entrySet()
                .stream()
                .map(entry -> entry.getKey().getRegisteredName() + ":" + entry.getIntValue())
                .sorted()
                .toList()
                .toString();
    }

    private static String describeCache() {
        if (teamProtectionLevels.isEmpty()) {
            return "{}";
        }

        return teamProtectionLevels.entrySet()
                .stream()
                .map(entry -> entry.getKey().debugName() + "=" + entry.getValue())
                .sorted()
                .toList()
                .toString();
    }
}
