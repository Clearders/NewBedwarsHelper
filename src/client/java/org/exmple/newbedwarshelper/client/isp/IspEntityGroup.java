package org.exmple.newbedwarshelper.client.isp;

import net.minecraft.world.entity.EntityType;

import java.util.List;

public record IspEntityGroup(String titleKey, List<EntityType<?>> entityTypes) {
}
