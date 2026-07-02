package org.exmple.newbedwarshelper.client.esp.entity;

import net.minecraft.world.entity.EntityType;

import java.util.List;

public record EspEntityGroup(String titleKey, List<EntityType<?>> entityTypes) {
}
