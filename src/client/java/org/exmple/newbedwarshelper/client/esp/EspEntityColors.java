package org.exmple.newbedwarshelper.client.esp;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

public final class EspEntityColors {
    private static final int RED = 0xFFFF4D4D;
    private static final int GREEN = 0xFF00FF00;
    private static final int CYAN = 0xFF00FFFF;
    private static final int YELLOW = 0xFFFFFF00;

    private EspEntityColors() {
    }

    public static int getOutlineColorOrDefault(Entity entity, int defaultColor) {
        if (entity instanceof Player || !EspTargetStorage.shouldGlow(entity)) {
            return defaultColor;
        }

        EntityType<?> entityType = entity.getType();
        if (EspEntityGroups.MONSTER.entityTypes().contains(entityType)) {
            return RED;
        }

        if (EspEntityGroups.CREATURE.entityTypes().contains(entityType)) {
            return GREEN;
        }

        if (entityType == EntityType.GLOW_SQUID) {
            return CYAN;
        }

        if (entityType == EntityType.DOLPHIN || entityType == EntityType.NAUTILUS) {
            return YELLOW;
        }

        return defaultColor;
    }
}
