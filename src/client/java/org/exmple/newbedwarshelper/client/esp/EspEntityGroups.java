package org.exmple.newbedwarshelper.client.esp;

import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public final class EspEntityGroups {
    public static final EspEntityGroup MONSTER = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.monster", List.of(
            EntityType.BLAZE,
            EntityType.BOGGED,
            EntityType.BREEZE,
            EntityType.CAMEL_HUSK,
            EntityType.CAVE_SPIDER,
            EntityType.CREAKING,
            EntityType.CREEPER,
            EntityType.DROWNED,
            EntityType.ELDER_GUARDIAN,
            EntityType.ENDER_DRAGON,
            EntityType.ENDERMAN,
            EntityType.ENDERMITE,
            EntityType.EVOKER,
            EntityType.GHAST,
            EntityType.GIANT,
            EntityType.GUARDIAN,
            EntityType.HOGLIN,
            EntityType.HUSK,
            EntityType.ILLUSIONER,
            EntityType.MAGMA_CUBE,
            EntityType.PARCHED,
            EntityType.PHANTOM,
            EntityType.PIGLIN,
            EntityType.PIGLIN_BRUTE,
            EntityType.PILLAGER,
            EntityType.RAVAGER,
            EntityType.SHULKER,
            EntityType.SILVERFISH,
            EntityType.SKELETON,
            EntityType.SLIME,
            EntityType.SPIDER,
            EntityType.STRAY,
            EntityType.VEX,
            EntityType.VINDICATOR,
            EntityType.WARDEN,
            EntityType.WITCH,
            EntityType.WITHER,
            EntityType.WITHER_SKELETON,
            EntityType.ZOGLIN,
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_HORSE,
            EntityType.ZOMBIE_NAUTILUS,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.ZOMBIFIED_PIGLIN
    ));

    public static final EspEntityGroup CREATURE = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.creature", List.of(
            EntityType.ALLAY,
            EntityType.ARMADILLO,
            EntityType.BEE,
            EntityType.CAMEL,
            EntityType.CAT,
            EntityType.CHICKEN,
            EntityType.COW,
            EntityType.DONKEY,
            EntityType.FOX,
            EntityType.FROG,
            EntityType.GOAT,
            EntityType.HAPPY_GHAST,
            EntityType.HORSE,
            EntityType.LLAMA,
            EntityType.MOOSHROOM,
            EntityType.MULE,
            EntityType.OCELOT,
            EntityType.PANDA,
            EntityType.PARROT,
            EntityType.PIG,
            EntityType.POLAR_BEAR,
            EntityType.RABBIT,
            EntityType.SHEEP,
            EntityType.SKELETON_HORSE,
            EntityType.SNIFFER,
            EntityType.STRIDER,
            EntityType.TADPOLE,
            EntityType.TRADER_LLAMA,
            EntityType.TURTLE,
            EntityType.WANDERING_TRADER,
            EntityType.WOLF
    ));

    public static final EspEntityGroup WATER_CREATURE = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.water_creature", List.of(
            EntityType.DOLPHIN,
            EntityType.NAUTILUS,
            EntityType.SQUID
    ));

    public static final EspEntityGroup UNDERGROUND_WATER_CREATURE = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.underground_water_creature", List.of(
            EntityType.GLOW_SQUID
    ));

    public static final EspEntityGroup WATER_AMBIENT = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.water_ambient", List.of(
            EntityType.COD,
            EntityType.PUFFERFISH,
            EntityType.SALMON,
            EntityType.TROPICAL_FISH
    ));

    public static final EspEntityGroup AXOLOTLS = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.axolotls", List.of(
            EntityType.AXOLOTL
    ));

    public static final EspEntityGroup AMBIENT = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.ambient", List.of(
            EntityType.BAT
    ));

    public static final List<EntityType<?>> BOAT_RAFT_TYPES = List.of(
            EntityType.ACACIA_BOAT,
            EntityType.ACACIA_CHEST_BOAT,
            EntityType.BAMBOO_RAFT,
            EntityType.BAMBOO_CHEST_RAFT,
            EntityType.BIRCH_BOAT,
            EntityType.BIRCH_CHEST_BOAT,
            EntityType.CHERRY_BOAT,
            EntityType.CHERRY_CHEST_BOAT,
            EntityType.DARK_OAK_BOAT,
            EntityType.DARK_OAK_CHEST_BOAT,
            EntityType.JUNGLE_BOAT,
            EntityType.JUNGLE_CHEST_BOAT,
            EntityType.MANGROVE_BOAT,
            EntityType.MANGROVE_CHEST_BOAT,
            EntityType.OAK_BOAT,
            EntityType.OAK_CHEST_BOAT,
            EntityType.PALE_OAK_BOAT,
            EntityType.PALE_OAK_CHEST_BOAT,
            EntityType.SPRUCE_BOAT,
            EntityType.SPRUCE_CHEST_BOAT
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
            EntityType.PLAYER,
            EntityType.ITEM,
            EntityType.ARMOR_STAND,
            EntityType.ARROW,
            EntityType.BLOCK_DISPLAY,
            EntityType.BREEZE_WIND_CHARGE,
            EntityType.CHEST_MINECART,
            EntityType.COMMAND_BLOCK_MINECART,
            EntityType.COPPER_GOLEM,
            EntityType.EGG,
            EntityType.ENDER_PEARL,
            EntityType.END_CRYSTAL,
            EntityType.EXPERIENCE_BOTTLE,
            EntityType.EYE_OF_ENDER,
            EntityType.FIREBALL,
            EntityType.FIREWORK_ROCKET,
            EntityType.FURNACE_MINECART,
            EntityType.GLOW_ITEM_FRAME,
            EntityType.HOPPER_MINECART,
            EntityType.IRON_GOLEM,
            EntityType.ITEM_DISPLAY,
            EntityType.ITEM_FRAME,
            EntityType.LEASH_KNOT,
            EntityType.LINGERING_POTION,
            EntityType.LLAMA_SPIT,
            EntityType.MINECART,
            EntityType.OMINOUS_ITEM_SPAWNER,
            EntityType.SHULKER_BULLET,
            EntityType.SMALL_FIREBALL,
            EntityType.SNOW_GOLEM,
            EntityType.SNOWBALL,
            EntityType.SPAWNER_MINECART,
            EntityType.SPECTRAL_ARROW,
            EntityType.SPLASH_POTION,
            EntityType.TNT,
            EntityType.TNT_MINECART,
            EntityType.TRIDENT,
            EntityType.VILLAGER,
            EntityType.WIND_CHARGE,
            EntityType.WITHER_SKULL
        ));
        entityTypes.addAll(3, BOAT_RAFT_TYPES);
        return List.copyOf(entityTypes);
    }
}
