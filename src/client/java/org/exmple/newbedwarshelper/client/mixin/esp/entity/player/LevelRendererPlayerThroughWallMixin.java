package org.exmple.newbedwarshelper.client.mixin.esp.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.exmple.newbedwarshelper.client.esp.entity.player.PlayerThroughWallRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererPlayerThroughWallMixin {
    @Inject(method = "submitEntities", at = @At("RETURN"))
    private void newbedwarshelper$capturePlayerModels(
            PoseStack poseStack,
            LevelRenderState levelState,
            SubmitNodeCollector output,
            CallbackInfo ci
    ) {
        PlayerThroughWallRenderer.submitAlwaysOnTop(Minecraft.getInstance(), levelState, output);
    }
}
