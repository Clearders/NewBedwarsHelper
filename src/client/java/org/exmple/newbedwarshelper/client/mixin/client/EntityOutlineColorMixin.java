package org.exmple.newbedwarshelper.client.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.exmple.newbedwarshelper.client.esp.EspEntityColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityOutlineColorMixin {
    @Inject(
            method = "extractRenderState",
            at = @At("RETURN")
    )
    private void applyEspOutlineColor(Entity entity, EntityRenderState state, float partialTicks, CallbackInfo ci) {
        state.outlineColor = EspEntityColors.getOutlineColorOrDefault(entity, state.outlineColor);
    }
}
