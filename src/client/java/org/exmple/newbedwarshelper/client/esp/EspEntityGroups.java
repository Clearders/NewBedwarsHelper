package org.exmple.newbedwarshelper.client.esp;

import net.minecraft.world.entity.EntityType;

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

    public static final EspEntityGroup MISC = new EspEntityGroup("screen.newbedwarshelper.esp_whitelist.group.misc", List.of(
            EntityType.PLAYER,
            EntityType.ARMOR_STAND,
            EntityType.COPPER_GOLEM,
            EntityType.IRON_GOLEM,
            EntityType.SNOW_GOLEM,
            EntityType.VILLAGER
    ));

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
}
