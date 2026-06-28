package org.exmple.newbedwarshelper.client.esp;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
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
        if (entity instanceof Player || entity.getType() == EntityTypes.MANNEQUIN || !EspTargetStorage.shouldGlow(entity)) {
            return defaultColor;
        }

        EntityType<?> entityType = entity.getType();
        if (entityType == EntityTypes.SULFUR_CUBE) {
            return GREEN;
        }

        if (EspEntityGroups.MONSTER.entityTypes().contains(entityType)) {
            return RED;
        }

        if (EspEntityGroups.CREATURE.entityTypes().contains(entityType)) {
            return GREEN;
        }

        if (entityType == EntityTypes.GLOW_SQUID) {
            return CYAN;
        }

        if (entityType == EntityTypes.DOLPHIN || entityType == EntityTypes.NAUTILUS) {
            return YELLOW;
        }

        if (isBoat(entityType)) {
            return LIGHT_WOOD;
        }

        if (entityType == EntityTypes.CHEST_MINECART) {
            return BROWN;
        }

        if (entityType == EntityTypes.HOPPER_MINECART) {
            return DARK_HOPPER;
        }

        if (entityType == EntityTypes.TNT || entityType == EntityTypes.TNT_MINECART) {
            return 0xFFFF0000;
        }

        if (entityType == EntityTypes.COMMAND_BLOCK_MINECART) {
            return COMMAND_BLOCK_FLESH;
        }

        if (entityType == EntityTypes.SPAWNER_MINECART || entityType == EntityTypes.END_CRYSTAL) {
            return PURPLE;
        }

        if (entityType == EntityTypes.ENDER_PEARL) {
            return DEEP_PEARL_GREEN;
        }

        if (entityType == EntityTypes.EYE_OF_ENDER || entityType == EntityTypes.SMALL_FIREBALL) {
            return YELLOW;
        }

        if (entityType == EntityTypes.FIREBALL) {
            return ORANGE;
        }

        if (entityType == EntityTypes.SPLASH_POTION || entityType == EntityTypes.LINGERING_POTION || entityType == EntityTypes.EXPERIENCE_BOTTLE) {
            return GOLD;
        }

        if (entityType == EntityTypes.TRIDENT) {
            return BLUE;
        }

        if (entityType == EntityTypes.FIREWORK_ROCKET) {
            return MAGENTA;
        }

        if (entityType == EntityTypes.WIND_CHARGE || entityType == EntityTypes.BREEZE_WIND_CHARGE) {
            return LIGHT_SKY_CYAN;
        }

        if (entityType == EntityTypes.OMINOUS_ITEM_SPAWNER) {
            return OMINOUS_CYAN;
        }

        if (entity instanceof WitherSkull witherSkull) {
            return witherSkull.isDangerous() ? DANGEROUS_WITHER_SKULL_CYAN : BLACK;
        }

        return defaultColor;
    }

    private static boolean isBoat(EntityType<?> entityType) {
        return entityType == EntityTypes.ACACIA_BOAT
                || entityType == EntityTypes.ACACIA_CHEST_BOAT
                || entityType == EntityTypes.BAMBOO_RAFT
                || entityType == EntityTypes.BAMBOO_CHEST_RAFT
                || entityType == EntityTypes.BIRCH_BOAT
                || entityType == EntityTypes.BIRCH_CHEST_BOAT
                || entityType == EntityTypes.CHERRY_BOAT
                || entityType == EntityTypes.CHERRY_CHEST_BOAT
                || entityType == EntityTypes.DARK_OAK_BOAT
                || entityType == EntityTypes.DARK_OAK_CHEST_BOAT
                || entityType == EntityTypes.JUNGLE_BOAT
                || entityType == EntityTypes.JUNGLE_CHEST_BOAT
                || entityType == EntityTypes.MANGROVE_BOAT
                || entityType == EntityTypes.MANGROVE_CHEST_BOAT
                || entityType == EntityTypes.OAK_BOAT
                || entityType == EntityTypes.OAK_CHEST_BOAT
                || entityType == EntityTypes.PALE_OAK_BOAT
                || entityType == EntityTypes.PALE_OAK_CHEST_BOAT
                || entityType == EntityTypes.SPRUCE_BOAT
                || entityType == EntityTypes.SPRUCE_CHEST_BOAT;
    }
}
