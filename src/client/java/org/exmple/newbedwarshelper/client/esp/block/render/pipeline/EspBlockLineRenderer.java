package org.exmple.newbedwarshelper.client.esp.block.render.pipeline;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.Optional;

public final class EspBlockLineRenderer {
    private static final Matrix4f IDENTITY_TEXTURE_MATRIX = new Matrix4f();

    private EspBlockLineRenderer() {
    }

    public static void render(EspBlockLineMesh mesh, Matrix4fc modelViewMatrix) {
        render(mesh, modelViewMatrix, EspBlockRenderPipelines.BLOCK_ESP_LINES);
    }

    public static void render(EspBlockLineMesh mesh, Matrix4fc modelViewMatrix, RenderPipeline pipeline) {
        if (mesh.isEmpty()) {
            return;
        }

        GpuBuffer vertexBuffer = mesh.uploadVertices();
        GpuBuffer indexBuffer = mesh.uploadIndices();
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(new Matrix4f(modelViewMatrix), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), IDENTITY_TEXTURE_MATRIX);
        RenderTarget mainRenderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "NewBedwarsHelper Block ESP",
                mainRenderTarget.getColorTextureView(),
                Optional.empty(),
                mainRenderTarget.getDepthTextureView(),
                OptionalDouble.empty()
        )) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            pass.setVertexBuffer(0, vertexBuffer.slice());
            pass.setIndexBuffer(indexBuffer, IndexType.INT);
            pass.drawIndexed(mesh.indexCount(), 1, 0, 0, 0);
        }
    }
}
