package org.exmple.newbedwarshelper.client.esp.block.render.pipeline;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;

public final class EspBlockRenderPipelines {
    public static final RenderPipeline BLOCK_ESP_LINES = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            .withLocation("pipeline/newbedwarshelper_block_esp_lines")
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.DEBUG_LINES)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .build());

    public static final RenderPipeline BLOCK_ESP_LINES_DEPTH_TEST = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            .withLocation("pipeline/newbedwarshelper_block_esp_lines_depth_test")
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.DEBUG_LINES)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .build());

    private EspBlockRenderPipelines() {
    }
}
