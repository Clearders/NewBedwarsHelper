package org.exmple.newbedwarshelper.client.statsfetcher;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.exmple.newbedwarshelper.client.utils.bedwars.BedwarsGameDetector;
import org.exmple.newbedwarshelper.client.utils.bedwars.BedwarsRosterResolver;
import org.exmple.newbedwarshelper.client.z_commands.statsfetcher.WeballCommand;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;

public final class StatsFetcherAutoWeballTrigger {
    private static final int MAX_WAIT_TICKS = 100;

    private static boolean wasInGame;
    private static boolean triggeredThisGame;
    private static boolean waitingForRoster;
    private static int waitTicks;

    private StatsFetcherAutoWeballTrigger() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(StatsFetcherAutoWeballTrigger::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        boolean inGame = BedwarsGameDetector.isInGame();
        if (!inGame) {
            reset();
            wasInGame = false;
            return;
        }

        if (!Boolean.TRUE.equals(ModConfig.getInstance().statsFetcher.autoWeballOnGameStart)) {
            wasInGame = true;
            return;
        }

        if (!wasInGame) {
            waitingForRoster = true;
            waitTicks = 0;
            triggeredThisGame = false;
        }
        wasInGame = true;

        if (triggeredThisGame || !waitingForRoster) {
            return;
        }

        waitTicks++;
        if (client.level == null || client.player == null || client.getConnection() == null) {
            return;
        }

        if (BedwarsGameDetector.getCurrentSelfTeamMarker(client).isEmpty()) {
            return;
        }

        BedwarsRosterResolver.Snapshot snapshot = BedwarsRosterResolver.resolve(client);
        if (snapshot.available() || waitTicks >= MAX_WAIT_TICKS) {
            if (WeballCommand.executeAutoWeball(client)) {
                triggeredThisGame = true;
                waitingForRoster = false;
            }
        }
    }

    private static void reset() {
        triggeredThisGame = false;
        waitingForRoster = false;
        waitTicks = 0;
    }
}
