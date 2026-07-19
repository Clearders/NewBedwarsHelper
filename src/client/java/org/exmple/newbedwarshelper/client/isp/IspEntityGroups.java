package org.exmple.newbedwarshelper.client.isp;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;

import java.util.ArrayList;
import java.util.List;

public final class IspEntityGroups {
    public static final IspEntityGroup MONSTER = group("monster", MobCategory.MONSTER);
    public static final IspEntityGroup CREATURE = group("creature", MobCategory.CREATURE);
    public static final IspEntityGroup WATER_CREATURE = group("water_creature", MobCategory.WATER_CREATURE);
    public static final IspEntityGroup UNDERGROUND_WATER_CREATURE = group("underground_water_creature", MobCategory.UNDERGROUND_WATER_CREATURE);
    public static final IspEntityGroup WATER_AMBIENT = group("water_ambient", MobCategory.WATER_AMBIENT);
    public static final IspEntityGroup AXOLOTLS = group("axolotls", MobCategory.AXOLOTLS);
    public static final IspEntityGroup AMBIENT = group("ambient", MobCategory.AMBIENT);
    public static final IspEntityGroup MISC = new IspEntityGroup(
            "screen.newbedwarshelper.isp_whitelist.group.misc",
            List.of(
                    EntityTypes.PLAYER,
                    EntityTypes.IRON_GOLEM,
                    EntityTypes.SNOW_GOLEM,
                    EntityTypes.MANNEQUIN
            )
    );

    public static final List<IspEntityGroup> ALL = List.of(
            MONSTER,
            CREATURE,
            WATER_CREATURE,
            UNDERGROUND_WATER_CREATURE,
            WATER_AMBIENT,
            AXOLOTLS,
            AMBIENT,
            MISC
    );

    private IspEntityGroups() {
    }

    public static List<EntityType<?>> allEntityTypes() {
        List<EntityType<?>> entityTypes = new ArrayList<>();
        for (IspEntityGroup group : ALL) {
            entityTypes.addAll(group.entityTypes());
        }
        return List.copyOf(entityTypes);
    }

    private static IspEntityGroup group(String name, MobCategory category) {
        return new IspEntityGroup(
                "screen.newbedwarshelper.isp_whitelist.group." + name,
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
}
