package org.exmple.newbedwarshelper.client.enemystatusviewer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.scores.PlayerTeam;
import org.exmple.newbedwarshelper.client.utils.bedwars.BedwarsGameDetector;
import org.exmple.newbedwarshelper.client.utils.bedwars.BedwarsTeamMarker;

public final class BedwarsProtectionTracker {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };
    private static final int CHECK_INTERVAL_TICKS = 20;

    private static final Map<BedwarsTeamMarker, ArmorUpgradeStatus> teamArmorStatuses = new HashMap<>();
    private static int ticksUntilNextCheck;

    private BedwarsProtectionTracker() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(BedwarsProtectionTracker::onClientTick);
    }

    public static OptionalInt getProtectionLevel(BedwarsTeamMarker marker) {
        ArmorUpgradeStatus status = teamArmorStatuses.get(marker);
        if (status == null || status.protectionLevel() <= 0) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(status.protectionLevel());
    }

    public static Optional<ArmorUpgradeStatus> getArmorStatus(BedwarsTeamMarker marker) {
        return Optional.ofNullable(teamArmorStatuses.get(marker));
    }

    public static void clear() {
        teamArmorStatuses.clear();
        ticksUntilNextCheck = 0;
    }

    private static void onClientTick(Minecraft client) {
        if (ticksUntilNextCheck > 0) {
            ticksUntilNextCheck--;
            return;
        }
        ticksUntilNextCheck = CHECK_INTERVAL_TICKS;

        if (!BedwarsGameDetector.isInGame()) {
            return;
        }

        if (client.level == null) {
            return;
        }

        Optional<BedwarsTeamMarker> selfMarker = BedwarsGameDetector.getCurrentSelfTeamMarker(client);
        if (selfMarker.isEmpty()) {
            return;
        }

        Holder<Enchantment> protection = client.level.registryAccess().getOrThrow(Enchantments.PROTECTION);
        Holder<Enchantment> featherFalling = client.level.registryAccess().getOrThrow(Enchantments.FEATHER_FALLING);
        for (AbstractClientPlayer player : client.level.players()) {
            Optional<BedwarsTeamMarker> marker = getPlayerTeamMarker(player);
            if (marker.isEmpty()) {
                continue;
            }

            ArmorUpgradeStatus status = marker.get().equals(selfMarker.get())
                    ? getSelfTeamArmorStatus(player, protection, featherFalling)
                    : getEnemyTeamArmorStatus(player);
            updateTeamArmorStatus(marker.get(), status);
        }
    }

    private static Optional<BedwarsTeamMarker> getPlayerTeamMarker(AbstractClientPlayer player) {
        if (!(player.getTeam() instanceof PlayerTeam team)) {
            return Optional.empty();
        }

        return BedwarsTeamMarker.fromColor(team.getColor());
    }

    private static ArmorUpgradeStatus getSelfTeamArmorStatus(
            AbstractClientPlayer player,
            Holder<Enchantment> protection,
            Holder<Enchantment> featherFalling
    ) {
        return new ArmorUpgradeStatus(
                getMaxProtectionLevel(player, protection),
                false,
                getFeatherFallingLevel(player, featherFalling)
        );
    }

    private static ArmorUpgradeStatus getEnemyTeamArmorStatus(AbstractClientPlayer player) {
        return new ArmorUpgradeStatus(0, hasFullArmorGlint(player), 0);
    }

    private static int getMaxProtectionLevel(AbstractClientPlayer player, Holder<Enchantment> protection) {
        int maxLevel = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            maxLevel = Math.max(maxLevel, EnchantmentHelper.getItemEnchantmentLevel(protection, stack));
        }

        return maxLevel;
    }

    private static int getFeatherFallingLevel(AbstractClientPlayer player, Holder<Enchantment> featherFalling) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        return Math.min(2, EnchantmentHelper.getItemEnchantmentLevel(featherFalling, boots));
    }

    private static boolean hasFullArmorGlint(AbstractClientPlayer player) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.hasFoil()) {
                return false;
            }
        }

        return true;
    }

    private static void updateTeamArmorStatus(BedwarsTeamMarker marker, ArmorUpgradeStatus observedStatus) {
        if (observedStatus.isEmpty()) {
            return;
        }

        teamArmorStatuses.merge(marker, observedStatus, ArmorUpgradeStatus::merge);
    }

    public record ArmorUpgradeStatus(int protectionLevel, boolean enemyFullArmorGlint, int featherFallingLevel) {
        private boolean isEmpty() {
            return protectionLevel <= 0 && !enemyFullArmorGlint && featherFallingLevel <= 0;
        }

        private ArmorUpgradeStatus merge(ArmorUpgradeStatus other) {
            return new ArmorUpgradeStatus(
                    Math.max(protectionLevel, other.protectionLevel),
                    enemyFullArmorGlint || other.enemyFullArmorGlint,
                    Math.max(featherFallingLevel, other.featherFallingLevel)
            );
        }
    }
}
