package org.exmple.newbedwarshelper.client.antiafk;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;

import java.util.Random;

public final class AntiAFKManager {
    private static final Random RANDOM = new Random();

    private static boolean featureEnabled = true;
    private static boolean enabled = false;
    private static boolean smallIcon = true;

    private static boolean wasActive = false;
    private static int activeMovementTicks = 0;
    private static int nextSmartMoveTick = 0;
    private static boolean isSmartMoving = false;
    private static Direction smartMoveDirection = Direction.FORWARD;
    private static int smartMoveDuration = 0;
    private static int smartMoveStartTick = 0;
    private static Vec3 smartMoveStartPos = null;

    private AntiAFKManager() {
    }

    private enum Direction {
        FORWARD,
        RIGHT,
        BACKWARD,
        LEFT
    }

    public static boolean isEnabled() {
        return featureEnabled && enabled;
    }

    public static boolean isFeatureEnabled() {
        return featureEnabled;
    }

    public static boolean isSmallIcon() {
        return smallIcon;
    }

    public static boolean shouldRenderHud() {
        return isEnabled();
    }

    public static boolean isControllingMovement() {
        return isEnabled() && isSmartMoving;
    }

    public static void setFeatureEnabled(boolean newState) {
        featureEnabled = newState;
        if (!featureEnabled) {
            setEnabled(false);
        }
        save();
    }

    public static void toggleFeatureEnabled() {
        setFeatureEnabled(!featureEnabled);
    }

    public static void setSmallIcon(boolean newState) {
        smallIcon = newState;
        save();
    }

    public static void toggleIconSize() {
        setSmallIcon(!smallIcon);
    }

    public static void setEnabled(boolean newState) {
        enabled = featureEnabled && newState;
        if (!enabled) {
            resetAndStop();
        }
    }

    public static void toggle() {
        if (!featureEnabled) {
            setEnabled(false);
            return;
        }
        setEnabled(!enabled);
    }

    public static void update(LocalPlayer player) {
        if (player == null) {
            resetAndStop();
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (!featureEnabled || !enabled) {
            resetAndStop();
            return;
        }
        if (mc.options == null) {
            return;
        }
        if (mc.screen != null) {
            forceStopAll(mc);
            return;
        }

        wasActive = true;
        activeMovementTicks++;

        if (nextSmartMoveTick == 0) {
            nextSmartMoveTick = activeMovementTicks + 20;
        }

        if (isSmartMoving) {
            if (activeMovementTicks >= smartMoveStartTick + smartMoveDuration) {
                isSmartMoving = false;
                forceStopAll(mc);
                boolean moved = hasMoved(player);
                nextSmartMoveTick = activeMovementTicks + (moved ? 1000 + RANDOM.nextInt(401) : 2);
                smartMoveStartPos = null;
            } else {
                applyDirection(mc, smartMoveDirection);
            }
        } else {
            if (activeMovementTicks >= nextSmartMoveTick) {
                startNewMove(mc, player);
            } else {
                forceStopAll(mc);
            }
        }
    }

    private static void startNewMove(Minecraft mc, LocalPlayer player) {
        isSmartMoving = true;
        smartMoveStartTick = activeMovementTicks;
        smartMoveStartPos = player.position();
        smartMoveDuration = 10 + RANDOM.nextInt(11);
        smartMoveDirection = Direction.values()[RANDOM.nextInt(Direction.values().length)];
        applyDirection(mc, smartMoveDirection);
    }

    private static void applyDirection(Minecraft mc, Direction direction) {
        if (mc.options == null) {
            return;
        }

        setKeyState(mc.options.keyUp, direction == Direction.FORWARD);
        setKeyState(mc.options.keyRight, direction == Direction.RIGHT);
        setKeyState(mc.options.keyDown, direction == Direction.BACKWARD);
        setKeyState(mc.options.keyLeft, direction == Direction.LEFT);
    }

    private static void forceStopAll(Minecraft mc) {
        if (mc.options == null) {
            return;
        }

        setKeyState(mc.options.keyUp, false);
        setKeyState(mc.options.keyRight, false);
        setKeyState(mc.options.keyDown, false);
        setKeyState(mc.options.keyLeft, false);
    }

    private static void setKeyState(KeyMapping key, boolean pressed) {
        if (key != null) {
            key.setDown(pressed);
        }
    }

    private static boolean hasMoved(LocalPlayer player) {
        if (smartMoveStartPos == null) {
            return false;
        }

        double dx = player.getX() - smartMoveStartPos.x;
        double dz = player.getZ() - smartMoveStartPos.z;
        return dx * dx + dz * dz > 1.0;
    }

    private static void resetAndStop() {
        if (wasActive) {
            forceStopAll(Minecraft.getInstance());
        }
        wasActive = false;
        isSmartMoving = false;
        activeMovementTicks = 0;
        nextSmartMoveTick = 0;
        smartMoveStartPos = null;
    }

    public static void init() {
        ModConfig.AntiAfkConfig config = ModConfig.getInstance().antiAfk;
        featureEnabled = config.featureEnabled;
        smallIcon = config.smallIcon;
        if (!featureEnabled) {
            enabled = false;
        }
    }

    private static void save() {
        ModConfig config = ModConfig.getInstance();
        config.antiAfk.featureEnabled = featureEnabled;
        config.antiAfk.smallIcon = smallIcon;
        config.save();
    }
}
