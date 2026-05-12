package org.exmple.newbedwarshelper.client.mixin.client;

import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.exmple.newbedwarshelper.client.hitboxenhance.HitboxEnhanceTargetStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public class HitboxEnhanceDebugRendererMixin {
    @Unique
    private static final int HITBOX_FILL_COLOR = 0x66CFCFCF;

    @Inject(method = "showHitboxes", at = @At("TAIL"))
    private void newbedwarshelper$renderFilledHitbox(Entity entity, float partialTicks, boolean isServerEntity, CallbackInfo ci) {
        if (!HitboxEnhanceTargetStorage.shouldEnhance(entity)) {
            return;
        }

        Vec3 latestPosition = entity.position();
        Vec3 currentPosition = entity.getPosition(partialTicks);
        Vec3 offset = currentPosition.subtract(latestPosition);
        Gizmos.cuboid(entity.getBoundingBox().move(offset), GizmoStyle.fill(HITBOX_FILL_COLOR));
    }
}
