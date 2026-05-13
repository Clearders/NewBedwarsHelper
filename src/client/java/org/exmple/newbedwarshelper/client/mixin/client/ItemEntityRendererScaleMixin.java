package org.exmple.newbedwarshelper.client.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import org.exmple.newbedwarshelper.client.itemmodelenhance.ItemEntityRenderScaleState;
import org.exmple.newbedwarshelper.client.itemmodelenhance.ItemScaleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererScaleMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void itemmodelenhance$markItemScale(ItemEntity itemEntity, ItemEntityRenderState itemEntityRenderState, float tickDelta, CallbackInfo ci) {
        float scale = ItemScaleRegistry.getScale(itemEntity.getItem().getItem());
        ((ItemEntityRenderScaleState) itemEntityRenderState).itemmodelenhance$setScale(scale);
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;submitMultipleFromCount(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ItemClusterRenderState;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/phys/AABB;)V"
            )
    )
    private void itemmodelenhance$scaleItems(
            ItemEntityRenderState itemEntityRenderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState,
            CallbackInfo ci
    ) {
        float scale = ((ItemEntityRenderScaleState) itemEntityRenderState).itemmodelenhance$getScale();
        if (scale != 1.0f) {
            poseStack.scale(scale, scale, scale);
        }
    }
}
