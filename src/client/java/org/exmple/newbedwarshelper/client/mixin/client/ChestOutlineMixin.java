package org.exmple.newbedwarshelper.client.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.exmple.newbedwarshelper.client.esp.EspBlockEntityTarget;
import org.exmple.newbedwarshelper.client.esp.EspTargetStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestRenderer.class)
public class ChestOutlineMixin {
    private static final int CHEST_OUTLINE = 0xFF8B5A2B;
    private static final int TRAPPED_CHEST_OUTLINE = 0xFFFF0000;
    private static final int ENDER_CHEST_OUTLINE = 0xFF8A2BE2;

    @Unique
    private ChestRenderState newbedwarshelper$currentChestState;

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD")
    )
    private void captureChestRenderState(ChestRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        this.newbedwarshelper$currentChestState = state;
    }

    @ModifyArg(
            method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
            ),
            index = 8
    )
    private int applyChestOutlineColor(int originalColor) {
        ChestRenderState state = this.newbedwarshelper$currentChestState;
        if (state == null) {
            return originalColor;
        }

        EspBlockEntityTarget target = getChestTarget(state);
        if (target == null || !EspTargetStorage.shouldGlowBlockEntity(target)) {
            return originalColor;
        }

        return switch (target) {
            case CHEST -> CHEST_OUTLINE;
            case TRAPPED_CHEST -> TRAPPED_CHEST_OUTLINE;
            case ENDER_CHEST -> ENDER_CHEST_OUTLINE;
            case SHULKER_BOX -> originalColor;
        };
    }

    private static EspBlockEntityTarget getChestTarget(ChestRenderState state) {
        return switch (state.material) {
            case REGULAR, CHRISTMAS, COPPER_UNAFFECTED, COPPER_EXPOSED, COPPER_WEATHERED, COPPER_OXIDIZED -> EspBlockEntityTarget.CHEST;
            case TRAPPED -> EspBlockEntityTarget.TRAPPED_CHEST;
            case ENDER_CHEST -> EspBlockEntityTarget.ENDER_CHEST;
        };
    }
}
