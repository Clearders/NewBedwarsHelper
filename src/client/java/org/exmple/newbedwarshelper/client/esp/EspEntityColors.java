package org.exmple.newbedwarshelper.client.esp;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;

public final class EspEntityColors {
    private static final int RED = 0xFFFF4D4D;
    private static final int GREEN = 0xFF00FF00;
    private static final int CYAN = 0xFF00FFFF;
    private static final int YELLOW = 0xFFFFFF00;
    private static final int BLACK = 0xFF000000;
    private static final int ORANGE = 0xFFFFA500;
    private static final int LIGHT_WOOD = 0xFFC99A5A;
    private static final int BROWN = 0xFF8B5A2B;
    private static final int DARK_HOPPER = 0xFF242424;
    private static final int COMMAND_BLOCK_FLESH = 0xFFD49A7A;
    private static final int PURPLE = 0xFF8A2BE2;
    private static final int DEEP_PEARL_GREEN = 0xFF007A63;
    private static final int GOLD = 0xFFFFD700;
    private static final int BLUE = 0xFF2F80ED;
    private static final int MAGENTA = 0xFFFF00FF;
    private static final int OMINOUS_CYAN = 0xFF16A6A6;
    private static final int DANGEROUS_WITHER_SKULL_CYAN = 0xFF72DADA;
    private static final int LIGHT_SKY_CYAN = 0xFF87CEEB;

    private EspEntityColors() {
    }

    public static int getOutlineColorOrDefault(Entity entity, int defaultColor) {
        if (entity instanceof Player || entity.getType() == EntityType.MANNEQUIN || !EspTargetStorage.shouldGlow(entity)) {
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

        if (isBoat(entityType)) {
            return LIGHT_WOOD;
        }

        if (entityType == EntityType.CHEST_MINECART) {
            return BROWN;
        }

        if (entityType == EntityType.HOPPER_MINECART) {
            return DARK_HOPPER;
        }

        if (entityType == EntityType.TNT || entityType == EntityType.TNT_MINECART) {
            return 0xFFFF0000;
        }

        if (entityType == EntityType.COMMAND_BLOCK_MINECART) {
            return COMMAND_BLOCK_FLESH;
        }

        if (entityType == EntityType.SPAWNER_MINECART || entityType == EntityType.END_CRYSTAL) {
            return PURPLE;
        }

        if (entityType == EntityType.ENDER_PEARL) {
            return DEEP_PEARL_GREEN;
        }

        if (entityType == EntityType.EYE_OF_ENDER || entityType == EntityType.SMALL_FIREBALL) {
            return YELLOW;
        }

        if (entityType == EntityType.FIREBALL) {
            return ORANGE;
        }

        if (entityType == EntityType.SPLASH_POTION || entityType == EntityType.LINGERING_POTION || entityType == EntityType.EXPERIENCE_BOTTLE) {
            return GOLD;
        }

        if (entityType == EntityType.TRIDENT) {
            return BLUE;
        }

        if (entityType == EntityType.FIREWORK_ROCKET) {
            return MAGENTA;
        }

        if (entityType == EntityType.WIND_CHARGE || entityType == EntityType.BREEZE_WIND_CHARGE) {
            return LIGHT_SKY_CYAN;
        }

        if (entityType == EntityType.OMINOUS_ITEM_SPAWNER) {
            return OMINOUS_CYAN;
        }

        if (entity instanceof WitherSkull witherSkull) {
            return witherSkull.isDangerous() ? DANGEROUS_WITHER_SKULL_CYAN : BLACK;
        }

        return defaultColor;
    }

    private static boolean isBoat(EntityType<?> entityType) {
        return entityType == EntityType.ACACIA_BOAT
                || entityType == EntityType.ACACIA_CHEST_BOAT
                || entityType == EntityType.BAMBOO_RAFT
                || entityType == EntityType.BAMBOO_CHEST_RAFT
                || entityType == EntityType.BIRCH_BOAT
                || entityType == EntityType.BIRCH_CHEST_BOAT
                || entityType == EntityType.CHERRY_BOAT
                || entityType == EntityType.CHERRY_CHEST_BOAT
                || entityType == EntityType.DARK_OAK_BOAT
                || entityType == EntityType.DARK_OAK_CHEST_BOAT
                || entityType == EntityType.JUNGLE_BOAT
                || entityType == EntityType.JUNGLE_CHEST_BOAT
                || entityType == EntityType.MANGROVE_BOAT
                || entityType == EntityType.MANGROVE_CHEST_BOAT
                || entityType == EntityType.OAK_BOAT
                || entityType == EntityType.OAK_CHEST_BOAT
                || entityType == EntityType.PALE_OAK_BOAT
                || entityType == EntityType.PALE_OAK_CHEST_BOAT
                || entityType == EntityType.SPRUCE_BOAT
                || entityType == EntityType.SPRUCE_CHEST_BOAT;
    }
}
