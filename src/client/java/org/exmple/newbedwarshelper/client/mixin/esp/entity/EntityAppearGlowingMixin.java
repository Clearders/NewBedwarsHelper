package org.exmple.newbedwarshelper.client.mixin.esp.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.exmple.newbedwarshelper.client.esp.entity.EspEntityStorage;
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
        if (!cir.getReturnValue() && EspEntityStorage.shouldGlow(entity)) {
            cir.setReturnValue(true);
        }
    }
}
