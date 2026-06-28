package org.exmple.newbedwarshelper.client.hitboxenhance;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;

import java.util.ArrayList;
import java.util.List;

public final class HitboxEnhanceEntityGroups {
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

    public static final HitboxEnhanceEntityGroup MONSTER = group("monster", MobCategory.MONSTER);
    public static final HitboxEnhanceEntityGroup CREATURE = group("creature", MobCategory.CREATURE);
    public static final HitboxEnhanceEntityGroup WATER_CREATURE = group("water_creature", MobCategory.WATER_CREATURE);
    public static final HitboxEnhanceEntityGroup UNDERGROUND_WATER_CREATURE = group("underground_water_creature", MobCategory.UNDERGROUND_WATER_CREATURE);
    public static final HitboxEnhanceEntityGroup WATER_AMBIENT = group("water_ambient", MobCategory.WATER_AMBIENT);
    public static final HitboxEnhanceEntityGroup AXOLOTLS = group("axolotls", MobCategory.AXOLOTLS);
    public static final HitboxEnhanceEntityGroup AMBIENT = group("ambient", MobCategory.AMBIENT);
    public static final HitboxEnhanceEntityGroup MISC = new HitboxEnhanceEntityGroup(
            "screen.newbedwarshelper.hitbox_enhance_whitelist.group.misc",
            createMiscTypes()
    );

    public static final List<HitboxEnhanceEntityGroup> ALL = List.of(
            MONSTER,
            CREATURE,
            WATER_CREATURE,
            UNDERGROUND_WATER_CREATURE,
            WATER_AMBIENT,
            AXOLOTLS,
            AMBIENT,
            MISC
    );

    private HitboxEnhanceEntityGroups() {
    }

    public static boolean isBoatRaftType(EntityType<?> entityType) {
        return BOAT_RAFT_TYPES.contains(entityType);
    }

    private static HitboxEnhanceEntityGroup group(String name, MobCategory category) {
        return new HitboxEnhanceEntityGroup(
                "screen.newbedwarshelper.hitbox_enhance_whitelist.group." + name,
                entityTypesForCategory(category)
        );
    }

    private static List<EntityType<?>> entityTypesForCategory(MobCategory category) {
        List<EntityType<?>> entityTypes = new ArrayList<>();
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (entityType.getCategory() == category) {
                entityTypes.add(entityType);
            }
        }
        return List.copyOf(entityTypes);
    }

    private static List<EntityType<?>> createMiscTypes() {
        List<EntityType<?>> entityTypes = new ArrayList<>();
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (entityType.getCategory() == MobCategory.MISC) {
                entityTypes.add(entityType);
            }
        }
        return List.copyOf(entityTypes);
    }
}
