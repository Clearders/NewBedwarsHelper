package org.exmple.newbedwarshelper.client.gammaoverride;

import org.exmple.newbedwarshelper.client.z_config.ModConfig;

public final class GammaOverrideManager {
    private static boolean enabled = false;
    private static ModConfig.GammaOverrideMode mode = ModConfig.GammaOverrideMode.NIGHT_VISION;

    private GammaOverrideManager() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean newState) {
        enabled = newState;
        save();
    }

    public static boolean toggleEnabled() {
        setEnabled(!enabled);
        return enabled;
    }

    public static ModConfig.GammaOverrideMode getMode() {
        return mode;
    }

    public static void setMode(ModConfig.GammaOverrideMode newMode) {
        mode = newMode == null ? ModConfig.GammaOverrideMode.NIGHT_VISION : newMode;
        save();
    }

    public static boolean isNightVisionMode() {
        return mode == ModConfig.GammaOverrideMode.NIGHT_VISION;
    }

    public static boolean isInvalidGammaMode() {
        return mode == ModConfig.GammaOverrideMode.INVALID_GAMMA;
    }

    public static void init() {
        ModConfig.GammaOverrideConfig config = ModConfig.getInstance().gammaOverride;
        enabled = config.enabled;
        mode = config.mode == null ? ModConfig.GammaOverrideMode.NIGHT_VISION : config.mode;
    }

    private static void save() {
        ModConfig config = ModConfig.getInstance();
        config.gammaOverride.enabled = enabled;
        config.gammaOverride.mode = mode;
        config.save();
    }
}
