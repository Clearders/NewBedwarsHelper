package org.exmple.newbedwarshelper.client.mixin.enemystatusviewer;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntitySharedFlagAccessor {
    @Invoker("getSharedFlag")
    boolean newbedwarshelper$getSharedFlag(int flag);
}
