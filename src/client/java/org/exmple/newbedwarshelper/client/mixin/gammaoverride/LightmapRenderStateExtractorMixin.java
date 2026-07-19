package org.exmple.newbedwarshelper.client.mixin.gammaoverride;

import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.exmple.newbedwarshelper.client.gammaoverride.GammaOverrideDebugLogger;
import org.exmple.newbedwarshelper.client.gammaoverride.GammaOverrideManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public class LightmapRenderStateExtractorMixin {
    @Inject(method = "extract", at = @At("TAIL"))
    private void newbedwarshelper$applyGammaOverride(LightmapRenderState lightmapRenderState, float partialTicks, CallbackInfo ci) {
        float brightnessBefore = lightmapRenderState.brightness;
        float nightVisionBefore = lightmapRenderState.nightVisionEffectIntensity;
        if (!GammaOverrideManager.isEnabled()) {
            return;
        }

        if (GammaOverrideManager.isNightVisionMode()) {
            lightmapRenderState.nightVisionEffectIntensity = 1.0F;
        } else if (GammaOverrideManager.isInvalidGammaMode()) {
            lightmapRenderState.brightness = 15.0F;
        }

        GammaOverrideDebugLogger.lightmap(lightmapRenderState, brightnessBefore, nightVisionBefore);
    }
}
