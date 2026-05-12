package org.exmple.newbedwarshelper.client.hitboxenhance;

import net.minecraft.world.entity.EntityType;

import java.util.List;

public record HitboxEnhanceEntityGroup(String titleKey, List<EntityType<?>> entityTypes) {
}
