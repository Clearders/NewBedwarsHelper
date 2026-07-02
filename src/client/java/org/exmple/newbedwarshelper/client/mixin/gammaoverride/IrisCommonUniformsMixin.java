package org.exmple.newbedwarshelper.client.mixin.gammaoverride;

import org.exmple.newbedwarshelper.client.gammaoverride.GammaOverrideIrisCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.irisshaders.iris.uniforms.CommonUniforms", remap = false)
public class IrisCommonUniformsMixin {
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "getNightVision", at = @At("HEAD"), cancellable = true)
    private static void newbedwarshelper$overrideIrisNightVision(CallbackInfoReturnable<Float> cir) {
        if (GammaOverrideIrisCompat.shouldApplyNightVisionUniformOverride()) {
            cir.setReturnValue(1.0F);
        }
    }
}
