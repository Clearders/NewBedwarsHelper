package org.exmple.newbedwarshelper.client.mixin.esp.entity;

import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import org.exmple.newbedwarshelper.client.esp.entity.EspEntityStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityOutlineMixin {
    private static final int ITEM_OUTLINE_COLOR = 0xFFFFFF00;

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void applyItemEspOutline(ItemEntity entity, ItemEntityRenderState state, float partialTicks, CallbackInfo ci) {
        if (state.outlineColor == 0 && EspEntityStorage.shouldGlowDroppedItem(entity)) {
            state.outlineColor = ITEM_OUTLINE_COLOR;
        }
    }
}
