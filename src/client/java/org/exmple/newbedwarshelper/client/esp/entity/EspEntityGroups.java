package org.exmple.newbedwarshelper.client.esp.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import java.util.ArrayList;
import java.util.List;

public final class EspEntityGroups {
    public static final EspEntityGroup MONSTER = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.monster", List.of(
            EntityTypes.BLAZE,
            EntityTypes.BOGGED,
            EntityTypes.BREEZE,
            EntityTypes.CAMEL_HUSK,
            EntityTypes.CAVE_SPIDER,
            EntityTypes.CREAKING,
            EntityTypes.CREEPER,
            EntityTypes.DROWNED,
            EntityTypes.ELDER_GUARDIAN,
            EntityTypes.ENDER_DRAGON,
            EntityTypes.ENDERMAN,
            EntityTypes.ENDERMITE,
            EntityTypes.EVOKER,
            EntityTypes.GHAST,
            EntityTypes.GIANT,
            EntityTypes.GUARDIAN,
            EntityTypes.HOGLIN,
            EntityTypes.HUSK,
            EntityTypes.ILLUSIONER,
            EntityTypes.MAGMA_CUBE,
            EntityTypes.PARCHED,
            EntityTypes.PHANTOM,
            EntityTypes.PIGLIN,
            EntityTypes.PIGLIN_BRUTE,
            EntityTypes.PILLAGER,
            EntityTypes.RAVAGER,
            EntityTypes.SHULKER,
            EntityTypes.SILVERFISH,
            EntityTypes.SKELETON,
            EntityTypes.SLIME,
            EntityTypes.SPIDER,
            EntityTypes.STRAY,
            EntityTypes.SULFUR_CUBE,
            EntityTypes.VEX,
            EntityTypes.VINDICATOR,
            EntityTypes.WARDEN,
            EntityTypes.WITCH,
            EntityTypes.WITHER,
            EntityTypes.WITHER_SKELETON,
            EntityTypes.ZOGLIN,
            EntityTypes.ZOMBIE,
            EntityTypes.ZOMBIE_HORSE,
            EntityTypes.ZOMBIE_NAUTILUS,
            EntityTypes.ZOMBIE_VILLAGER,
            EntityTypes.ZOMBIFIED_PIGLIN
    ));

    public static final EspEntityGroup CREATURE = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.creature", List.of(
            EntityTypes.ALLAY,
            EntityTypes.ARMADILLO,
            EntityTypes.BEE,
            EntityTypes.CAMEL,
            EntityTypes.CAT,
            EntityTypes.CHICKEN,
            EntityTypes.COW,
            EntityTypes.DONKEY,
            EntityTypes.FOX,
            EntityTypes.FROG,
            EntityTypes.GOAT,
            EntityTypes.HAPPY_GHAST,
            EntityTypes.HORSE,
            EntityTypes.LLAMA,
            EntityTypes.MOOSHROOM,
            EntityTypes.MULE,
            EntityTypes.OCELOT,
            EntityTypes.PANDA,
            EntityTypes.PARROT,
            EntityTypes.PIG,
            EntityTypes.POLAR_BEAR,
            EntityTypes.RABBIT,
            EntityTypes.SHEEP,
            EntityTypes.SKELETON_HORSE,
            EntityTypes.SNIFFER,
            EntityTypes.STRIDER,
            EntityTypes.TADPOLE,
            EntityTypes.TRADER_LLAMA,
            EntityTypes.TURTLE,
            EntityTypes.WANDERING_TRADER,
            EntityTypes.WOLF
    ));

    public static final EspEntityGroup WATER_CREATURE = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.water_creature", List.of(
            EntityTypes.DOLPHIN,
            EntityTypes.NAUTILUS,
            EntityTypes.SQUID
    ));

    public static final EspEntityGroup UNDERGROUND_WATER_CREATURE = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.underground_water_creature", List.of(
            EntityTypes.GLOW_SQUID
    ));

    public static final EspEntityGroup WATER_AMBIENT = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.water_ambient", List.of(
            EntityTypes.COD,
            EntityTypes.PUFFERFISH,
            EntityTypes.SALMON,
            EntityTypes.TROPICAL_FISH
    ));

    public static final EspEntityGroup AXOLOTLS = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.axolotls", List.of(
            EntityTypes.AXOLOTL
    ));

    public static final EspEntityGroup AMBIENT = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.ambient", List.of(
            EntityTypes.BAT
    ));

    public static final List<EntityType<?>> BOAT_RAFT_TYPES = List.of(
            EntityTypes.ACACIA_BOAT,
            EntityTypes.ACACIA_CHEST_BOAT,
            EntityTypes.BAMBOO_RAFT,
            EntityTypes.BAMBOO_CHEST_RAFT,
            EntityTypes.BIRCH_BOAT,
            EntityTypes.BIRCH_CHEST_BOAT,
            EntityTypes.CHERRY_BOAT,
            EntityTypes.CHERRY_CHEST_BOAT,
            EntityTypes.DARK_OAK_BOAT,
            EntityTypes.DARK_OAK_CHEST_BOAT,
            EntityTypes.JUNGLE_BOAT,
            EntityTypes.JUNGLE_CHEST_BOAT,
            EntityTypes.MANGROVE_BOAT,
            EntityTypes.MANGROVE_CHEST_BOAT,
            EntityTypes.OAK_BOAT,
            EntityTypes.OAK_CHEST_BOAT,
            EntityTypes.PALE_OAK_BOAT,
            EntityTypes.PALE_OAK_CHEST_BOAT,
            EntityTypes.SPRUCE_BOAT,
            EntityTypes.SPRUCE_CHEST_BOAT
    );

    public static final EspEntityGroup MISC = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.misc", createMiscTypes());

    public static final List<EspEntityGroup> ALL = List.of(
            MONSTER,
            CREATURE,
            WATER_CREATURE,
            UNDERGROUND_WATER_CREATURE,
            WATER_AMBIENT,
            AXOLOTLS,
            AMBIENT,
            MISC
    );

    private EspEntityGroups() {
    }

    public static boolean isBoatRaftType(EntityType<?> entityType) {
        return BOAT_RAFT_TYPES.contains(entityType);
    }

    private static List<EntityType<?>> createMiscTypes() {
        List<EntityType<?>> entityTypes = new ArrayList<>(List.of(
            EntityTypes.PLAYER,
            EntityTypes.ITEM,
            EntityTypes.ARMOR_STAND,
            EntityTypes.ARROW,
            EntityTypes.BLOCK_DISPLAY,
            EntityTypes.BREEZE_WIND_CHARGE,
            EntityTypes.CHEST_MINECART,
            EntityTypes.COMMAND_BLOCK_MINECART,
            EntityTypes.COPPER_GOLEM,
            EntityTypes.EGG,
            EntityTypes.ENDER_PEARL,
            EntityTypes.END_CRYSTAL,
            EntityTypes.EXPERIENCE_BOTTLE,
            EntityTypes.EYE_OF_ENDER,
            EntityTypes.FIREBALL,
            EntityTypes.FIREWORK_ROCKET,
            EntityTypes.FURNACE_MINECART,
            EntityTypes.GLOW_ITEM_FRAME,
            EntityTypes.HOPPER_MINECART,
            EntityTypes.IRON_GOLEM,
            EntityTypes.ITEM_DISPLAY,
            EntityTypes.ITEM_FRAME,
            EntityTypes.LEASH_KNOT,
            EntityTypes.LINGERING_POTION,
            EntityTypes.LLAMA_SPIT,
            EntityTypes.MANNEQUIN,
            EntityTypes.MINECART,
            EntityTypes.OMINOUS_ITEM_SPAWNER,
            EntityTypes.SHULKER_BULLET,
            EntityTypes.SMALL_FIREBALL,
            EntityTypes.SNOW_GOLEM,
            EntityTypes.SNOWBALL,
            EntityTypes.SPAWNER_MINECART,
            EntityTypes.SPECTRAL_ARROW,
            EntityTypes.SPLASH_POTION,
            EntityTypes.TNT,
            EntityTypes.TNT_MINECART,
            EntityTypes.TRIDENT,
            EntityTypes.VILLAGER,
            EntityTypes.WIND_CHARGE,
            EntityTypes.WITHER_SKULL
        ));
        entityTypes.addAll(3, BOAT_RAFT_TYPES);
        return List.copyOf(entityTypes);
    }
}
