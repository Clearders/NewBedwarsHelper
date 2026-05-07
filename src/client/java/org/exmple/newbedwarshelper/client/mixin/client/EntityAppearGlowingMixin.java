package org.exmple.newbedwarshelper.client.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.exmple.newbedwarshelper.client.esp.EspTargetStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class EntityAppearGlowingMixin {
    @Inject(
            method = "shouldEntityAppearGlowing",
            at = @At("RETURN"),
            cancellable = true
    )
    private void applyEspGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && EspTargetStorage.shouldGlow(entity)) {
            cir.setReturnValue(true);
        }
    }
}
