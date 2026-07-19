package org.exmple.newbedwarshelper.client.mixin.esp.entity;

import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.client.renderer.entity.state.WitherSkullRenderState;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import org.exmple.newbedwarshelper.client.esp.entity.EspEntityStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherSkullRenderer.class)
public class WitherSkullOutlineMixin {
    private static final int WITHER_SKULL_OUTLINE_COLOR = 0xFF000000;
    private static final int DANGEROUS_WITHER_SKULL_OUTLINE_COLOR = 0xFF72DADA;

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void applyWitherSkullEspOutline(WitherSkull entity, WitherSkullRenderState state, float partialTicks, CallbackInfo ci) {
        if (!EspEntityStorage.shouldGlow(entity)) {
            return;
        }

        state.outlineColor = entity.isDangerous() ? DANGEROUS_WITHER_SKULL_OUTLINE_COLOR : WITHER_SKULL_OUTLINE_COLOR;
    }
}
