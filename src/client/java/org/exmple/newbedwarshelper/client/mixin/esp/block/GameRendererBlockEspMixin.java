package org.exmple.newbedwarshelper.client.mixin.esp.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.exmple.newbedwarshelper.client.esp.block.render.EspBlockRenderer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererBlockEspMixin {
    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=hand"))
    private void newbedwarshelper$renderBlockEsp(
            DeltaTracker deltaTracker,
            CallbackInfo ci,
            @Local(name = "projectionMatrix") Matrix4f projectionMatrix,
            @Local(name = "modelViewMatrix") Matrix4fc modelViewMatrix
    ) {
        EspBlockRenderer.render(Minecraft.getInstance(), projectionMatrix, modelViewMatrix);
    }
}
