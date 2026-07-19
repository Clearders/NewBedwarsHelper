package org.exmple.newbedwarshelper.client.esp.entity.player;

import net.minecraft.world.entity.EntityTypes;
import org.exmple.newbedwarshelper.client.esp.EspGlobalState;
import org.exmple.newbedwarshelper.client.esp.entity.EspEntityStorage;
import org.exmple.newbedwarshelper.client.z_config.ModConfig;

public final class PlayerThroughWallEsp {
    private PlayerThroughWallEsp() {
    }

    public static boolean isConfiguredEnabled() {
        return Boolean.TRUE.equals(ModConfig.getInstance().esp.showPlayerModelsThroughWalls);
    }

    public static boolean shouldRender() {
        return EspGlobalState.isEnabled()
                && isConfiguredEnabled()
                && EspEntityStorage.isEntityTypeEspEnabled(EntityTypes.PLAYER);
    }

    public static void setConfiguredEnabled(boolean enabled) {
        ModConfig config = ModConfig.getInstance();
        config.esp.showPlayerModelsThroughWalls = enabled;
        config.save();
    }
}
