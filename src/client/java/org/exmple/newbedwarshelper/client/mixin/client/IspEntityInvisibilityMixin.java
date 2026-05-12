package org.exmple.newbedwarshelper.client.mixin.client;

import net.minecraft.world.entity.Entity;
import org.exmple.newbedwarshelper.client.isp.IspTargetStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class IspEntityInvisibilityMixin {
    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    private void newbedwarshelper$forceInvisibleFalseForIsp(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (IspTargetStorage.shouldForceVisible(entity)) {
            cir.setReturnValue(false);
        }
    }
}
