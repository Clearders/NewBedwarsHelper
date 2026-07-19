package org.exmple.newbedwarshelper.client.mixin.esp.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.WindChargeRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.exmple.newbedwarshelper.client.esp.entity.EspEntityRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WindChargeRenderer.class)
public class WindChargeOutlineMixin {
    @Redirect(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;breezeWind(Lnet/minecraft/resources/Identifier;FF)Lnet/minecraft/client/renderer/rendertype/RenderType;"
            )
    )
    private RenderType useOutlineCompatibleBreezeWind(Identifier texture, float uOffset, float vOffset, EntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.outlineColor == 0) {
            return RenderTypes.breezeWind(texture, uOffset, vOffset);
        }

        return EspEntityRenderTypes.breezeWindWithOutline(texture, uOffset, vOffset);
    }
}
