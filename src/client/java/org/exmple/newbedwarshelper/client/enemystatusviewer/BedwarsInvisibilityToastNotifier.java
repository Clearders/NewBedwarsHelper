package org.exmple.newbedwarshelper.client.enemystatusviewer;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import org.exmple.newbedwarshelper.client.gui.MultilineSystemToast;
import org.exmple.newbedwarshelper.client.mixin.client.EntitySharedFlagAccessor;

public final class BedwarsInvisibilityToastNotifier {
    private static final int ENTITY_INVISIBLE_SHARED_FLAG = 5;
    private static final String TITLE_KEY = "toast.newbedwarshelper.enemy_invis.title";
    private static final String MESSAGE_KEY = "toast.newbedwarshelper.enemy_invis.message";
    private static final int SCAN_INTERVAL_TICKS = 20;

    private static final Set<UUID> invisiblePlayers = new HashSet<>();
    private static final Set<UUID> ignoredNegativeDurationPlayers = new HashSet<>();
    private static int ticksUntilNextScan;

    private BedwarsInvisibilityToastNotifier() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(BedwarsInvisibilityToastNotifier::onClientTick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
    }

    public static void onMobEffectUpdated(ClientboundUpdateMobEffectPacket packet) {
        if (!packet.getEffect().equals(MobEffects.INVISIBILITY)) {
            return;
        }

        BedwarsDebugLogger.invisibility("received invisibility update packet; entityId="
                + packet.getEntityId() + ", durationTicks=" + packet.getEffectDurationTicks());
        if (!BedwarsGameDetector.isInGame()) {
            BedwarsDebugLogger.invisibility("skip update: detector says not in game");
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            BedwarsDebugLogger.invisibility("skip update: level or player is null");
            return;
        }

        Entity entity = client.level.getEntity(packet.getEntityId());
        if (!(entity instanceof AbstractClientPlayer player) || player == client.player) {
            BedwarsDebugLogger.invisibility("skip update: entity is not another client player; entity="
                    + describeEntity(entity)
                    + ", isSelf=" + (entity == client.player));
            return;
        }

        if (packet.getEffectDurationTicks() < 0) {
            ignoredNegativeDurationPlayers.add(player.getUUID());
            invisiblePlayers.remove(player.getUUID());
            BedwarsDebugLogger.invisibility("skip update: negative duration invisibility; player="
                    + player.getGameProfile().name()
                    + ", uuid=" + player.getUUID()
                    + ", durationTicks=" + packet.getEffectDurationTicks());
            return;
        }
        ignoredNegativeDurationPlayers.remove(player.getUUID());

        if (!isListed(player)) {
            BedwarsDebugLogger.invisibility("skip update: player is not listed; player="
                    + player.getGameProfile().name()
                    + ", uuid=" + player.getUUID());
            invisiblePlayers.remove(player.getUUID());
            return;
        }

        Optional<BedwarsTeamMarker> selfMarker = BedwarsGameDetector.getCurrentSelfTeamMarker(client);
        if (selfMarker.isEmpty()) {
            BedwarsDebugLogger.invisibility("skip update: self BedWars marker is unavailable");
            return;
        }

        PlayerInvisibilityTarget target = getEnemyTarget(client, player, selfMarker.get());
        if (target == null) {
            return;
        }

        boolean hasInvisibilityEffect = player.hasEffect(MobEffects.INVISIBILITY);
        boolean rawSharedInvisible = isRawSharedInvisible(player);
        BedwarsDebugLogger.invisibility("update state: player="
                + player.getGameProfile().name()
                + ", marker=" + target.marker.debugName()
                + ", durationTicks=" + packet.getEffectDurationTicks()
                + ", hasInvisibilityEffect=" + hasInvisibilityEffect
                + ", rawSharedInvisible=" + rawSharedInvisible
                + ", isInvisibleAfterMixins=" + player.isInvisible());
        if (hasInvisibilityEffect || rawSharedInvisible) {
            showToastOnce(client, player, target.marker, "packet");
        }
    }

    public static void onMobEffectRemoved(ClientboundRemoveMobEffectPacket packet) {
        if (!packet.effect().equals(MobEffects.INVISIBILITY)) {
            return;
        }

        BedwarsDebugLogger.invisibility("received invisibility remove packet");
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            BedwarsDebugLogger.invisibility("skip remove: level is null");
            return;
        }

        Entity entity = packet.getEntity(client.level);
        if (entity != null) {
            boolean removed = invisiblePlayers.remove(entity.getUUID());
            ignoredNegativeDurationPlayers.remove(entity.getUUID());
            BedwarsDebugLogger.invisibility("remove invisible marker: entity="
                    + describeEntity(entity)
                    + ", removed=" + removed);
        } else {
            BedwarsDebugLogger.invisibility("skip remove: packet entity not found in level");
        }
    }

    public static void clear() {
        invisiblePlayers.clear();
        ignoredNegativeDurationPlayers.clear();
        ticksUntilNextScan = 0;
    }

    private static void onClientTick(Minecraft client) {
        if (ticksUntilNextScan > 0) {
            ticksUntilNextScan--;
            return;
        }
        ticksUntilNextScan = SCAN_INTERVAL_TICKS;

        if (!BedwarsGameDetector.isInGame()) {
            if (!invisiblePlayers.isEmpty()) {
                BedwarsDebugLogger.invisibility("clear scan cache: detector says not in game");
            }
            invisiblePlayers.clear();
            return;
        }

        if (client.level == null || client.player == null) {
            if (!invisiblePlayers.isEmpty()) {
                BedwarsDebugLogger.invisibility("clear scan cache: level or player is null");
            }
            invisiblePlayers.clear();
            return;
        }

        Optional<BedwarsTeamMarker> selfMarker = BedwarsGameDetector.getCurrentSelfTeamMarker(client);
        if (selfMarker.isEmpty()) {
            if (!invisiblePlayers.isEmpty()) {
                BedwarsDebugLogger.invisibility("clear scan cache: self BedWars marker is unavailable");
            }
            invisiblePlayers.clear();
            return;
        }

        Set<UUID> currentlyInvisible = new HashSet<>();
        for (AbstractClientPlayer player : client.level.players()) {
            if (player == client.player) {
                continue;
            }

            if (!isListed(player)) {
                invisiblePlayers.remove(player.getUUID());
                ignoredNegativeDurationPlayers.remove(player.getUUID());
                continue;
            }

            if (ignoredNegativeDurationPlayers.contains(player.getUUID())) {
                invisiblePlayers.remove(player.getUUID());
                BedwarsDebugLogger.invisibility("skip scan: negative duration invisibility is ignored; player="
                        + player.getGameProfile().name()
                        + ", uuid=" + player.getUUID());
                continue;
            }

            PlayerInvisibilityTarget target = getEnemyTarget(client, player, selfMarker.get());
            if (target == null) {
                invisiblePlayers.remove(player.getUUID());
                continue;
            }

            boolean hasInvisibilityEffect = player.hasEffect(MobEffects.INVISIBILITY);
            boolean rawSharedInvisible = isRawSharedInvisible(player);
            boolean invisible = hasInvisibilityEffect || rawSharedInvisible;
            BedwarsDebugLogger.invisibility("scan player: name="
                    + player.getGameProfile().name()
                    + ", uuid=" + player.getUUID()
                    + ", id=" + player.getId()
                    + ", marker=" + target.marker.debugName()
                    + ", hasInvisibilityEffect=" + hasInvisibilityEffect
                    + ", rawSharedInvisible=" + rawSharedInvisible
                    + ", isInvisibleAfterMixins=" + player.isInvisible());
            if (!invisible) {
                invisiblePlayers.remove(player.getUUID());
                continue;
            }

            currentlyInvisible.add(player.getUUID());
            showToastOnce(client, player, target.marker, "scan");
        }

        invisiblePlayers.retainAll(currentlyInvisible);
    }

    private static PlayerInvisibilityTarget getEnemyTarget(Minecraft client, AbstractClientPlayer player, BedwarsTeamMarker selfMarker) {
        if (!(player.getTeam() instanceof PlayerTeam playerTeam)) {
            BedwarsDebugLogger.invisibility("skip player: no valid scoreboard team; player="
                    + player.getGameProfile().name()
                    + ", playerTeam=" + describeTeam(player.getTeam())
                    + ", selfTeam=" + describeTeam(client.player.getTeam()));
            return null;
        }

        BedwarsTeamMarker marker = BedwarsTeamMarker.fromColor(playerTeam.getColor()).orElse(null);
        if (marker == null) {
            BedwarsDebugLogger.invisibility("skip player: team color is not a BedWars marker; player="
                    + player.getGameProfile().name()
                    + ", team=" + describeTeam(playerTeam));
            return null;
        }

        if (marker.equals(selfMarker)) {
            BedwarsDebugLogger.invisibility("skip player: same BedWars marker; player="
                    + player.getGameProfile().name()
                    + ", marker=" + marker.debugName()
                    + ", selfMarker=" + selfMarker.debugName()
                    + ", playerTeam=" + describeTeam(playerTeam)
                    + ", selfTeam=" + describeTeam(client.player.getTeam()));
            return null;
        }

        BedwarsDebugLogger.invisibility("enemy target accepted: player="
                + player.getGameProfile().name()
                + ", marker=" + marker.debugName()
                + ", selfMarker=" + selfMarker.debugName()
                + ", playerTeam=" + describeTeam(playerTeam)
                + ", selfTeam=" + describeTeam(client.player.getTeam()));
        return new PlayerInvisibilityTarget(marker);
    }

    private static void showToastOnce(Minecraft client, AbstractClientPlayer player, BedwarsTeamMarker marker, String source) {
        if (!invisiblePlayers.add(player.getUUID())) {
            return;
        }

        BedwarsDebugLogger.invisibility("show toast from " + source + ": player="
                + player.getGameProfile().name()
                + ", uuid=" + player.getUUID()
                + ", id=" + player.getId()
                + ", marker=" + marker.debugName());
        showToast(client, player, marker);
    }

    private static boolean isRawSharedInvisible(Entity entity) {
        return ((EntitySharedFlagAccessor) entity).newbedwarshelper$getSharedFlag(ENTITY_INVISIBLE_SHARED_FLAG);
    }

    private static boolean isListed(AbstractClientPlayer player) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return false;
        }

        for (PlayerInfo info : connection.getListedOnlinePlayers()) {
            if (info.getProfile().id().equals(player.getUUID())) {
                return true;
            }
        }

        return false;
    }

    private static void showToast(Minecraft client, AbstractClientPlayer player, BedwarsTeamMarker marker) {
        Component playerName = Component.literal(player.getGameProfile().name()).withStyle(marker.color());
        MultilineSystemToast.add(
                client,
                Component.translatable(TITLE_KEY),
                Component.translatable(MESSAGE_KEY, playerName)
        );
    }

    private record PlayerInvisibilityTarget(BedwarsTeamMarker marker) {
    }

    private static String describeEntity(Entity entity) {
        if (entity == null) {
            return "null";
        }

        return entity.getClass().getSimpleName()
                + "{name=" + entity.getName().getString()
                + ", uuid=" + entity.getUUID()
                + ", id=" + entity.getId() + "}";
    }

    private static String describeTeam(net.minecraft.world.scores.Team team) {
        if (team == null) {
            return "null";
        }

        return "{name=" + team.getName() + ", color=" + team.getColor().getName() + "}";
    }
}
