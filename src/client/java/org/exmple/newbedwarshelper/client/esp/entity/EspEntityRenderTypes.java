package org.exmple.newbedwarshelper.client.esp.entity;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.resources.Identifier;

public final class EspEntityRenderTypes {
    private EspEntityRenderTypes() {
    }

    public static RenderType breezeWindWithOutline(Identifier texture, float uOffset, float vOffset) {
        return RenderType.create(
                "newbedwarshelper_breeze_wind_outline",
                RenderSetup.builder(RenderPipelines.BREEZE_WIND)
                        .withTexture("Sampler0", texture)
                        .setTextureTransform(new TextureTransform.OffsetTextureTransform(uOffset, vOffset))
                        .useLightmap()
                        .sortOnUpload()
                        .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                        .createRenderSetup()
        );
    }
}
