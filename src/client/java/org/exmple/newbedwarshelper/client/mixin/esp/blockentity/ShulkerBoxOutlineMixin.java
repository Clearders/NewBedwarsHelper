package org.exmple.newbedwarshelper.client.mixin.esp.blockentity;

import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import org.exmple.newbedwarshelper.client.esp.blockentity.EspBlockEntityTarget;
import org.exmple.newbedwarshelper.client.esp.blockentity.EspBlockEntityStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ShulkerBoxRenderer.class)
public class ShulkerBoxOutlineMixin {
    private static final int SHULKER_BOX_OUTLINE = 0xFFAA80D8;

    @ModifyArg(
            method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ShulkerBoxRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/ShulkerBoxRenderer;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IILnet/minecraft/core/Direction;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;Lnet/minecraft/client/resources/model/sprite/SpriteId;I)V"
            ),
            index = 8
    )
    private int applyShulkerBoxOutlineColor(int originalColor) {
        return EspBlockEntityStorage.shouldGlowBlockEntity(EspBlockEntityTarget.SHULKER_BOX) ? SHULKER_BOX_OUTLINE : originalColor;
    }
}
