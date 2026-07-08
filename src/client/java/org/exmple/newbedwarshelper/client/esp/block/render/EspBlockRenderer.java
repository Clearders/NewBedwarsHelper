package org.exmple.newbedwarshelper.client.esp.block.render;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.exmple.newbedwarshelper.client.esp.block.render.navigation.EspBlockNavigationConstants;
import org.exmple.newbedwarshelper.client.esp.block.render.navigation.EspBlockNavigationController;
import org.exmple.newbedwarshelper.client.esp.block.render.navigation.EspBlockTracerRenderer;
import net.minecraft.world.phys.AABB;
import org.exmple.newbedwarshelper.client.esp.block.render.pipeline.EspBlockLineMesh;
import org.exmple.newbedwarshelper.client.esp.block.render.pipeline.EspBlockLineRenderer;
import org.exmple.newbedwarshelper.client.esp.block.render.pipeline.EspBlockRenderPipelines;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

public final class EspBlockRenderer {
    private static final int MAX_LINES_PER_FRAME = 1_500_000;

    private static final EspBlockLineMesh THROUGH_WALL_MESH = new EspBlockLineMesh();

    private EspBlockRenderer() {
    }

    public static void render(Minecraft client, Matrix4fc projectionMatrix, Matrix4fc modelViewMatrix) {
        if (!EspBlockEspController.shouldRender(client)) {
            return;
        }

        THROUGH_WALL_MESH.begin();
        EspBlockNavigationController.update(client);
        LineSink throughWallSink = new LineSink(client, THROUGH_WALL_MESH);
        EspBlockEspController.forEachEntry(entry -> {
            if (!EspBlockNavigationController.shouldUseAimedNavigationColor(entry)) {
                emitEntry(entry, throughWallSink);
            }
        });
        EspBlockTracerRenderer.render(throughWallSink, EspBlockNavigationController.nearestGroup(), screenCenterWorld(projectionMatrix, modelViewMatrix, throughWallSink.cameraPos()));
        EspBlockLineRenderer.render(THROUGH_WALL_MESH, modelViewMatrix, EspBlockRenderPipelines.BLOCK_ESP_LINES);
    }

    private static void emitEntry(EspBlockCacheEntry entry, LineSink sink) {
        int neighbours = entry.neighbours();
        AABB bounds = entry.bounds();
        double x1 = entry.pos().getX() + bounds.minX;
        double y1 = entry.pos().getY() + bounds.minY;
        double z1 = entry.pos().getZ() + bounds.minZ;
        double x2 = entry.pos().getX() + bounds.maxX;
        double y2 = entry.pos().getY() + bounds.maxY;
        double z2 = entry.pos().getZ() + bounds.maxZ;
        int color = entry.color();
        if (EspBlockNavigationController.shouldUseNavigationColor(entry)) {
            color = EspBlockNavigationConstants.NAVIGATION_COLOR;
        }

        sink.renderedBlocks++;
        if (neighbours == 0) {
            emitFullBox(sink, x1, y1, z1, x2, y2, z2, color);
            return;
        }

        if (((neighbours & EspBlockNeighbourFlags.LE) != EspBlockNeighbourFlags.LE && (neighbours & EspBlockNeighbourFlags.BA) != EspBlockNeighbourFlags.BA)
                || ((neighbours & EspBlockNeighbourFlags.LE) == EspBlockNeighbourFlags.LE && (neighbours & EspBlockNeighbourFlags.BA) == EspBlockNeighbourFlags.BA && (neighbours & EspBlockNeighbourFlags.BA_LE) != EspBlockNeighbourFlags.BA_LE)) {
            sink.line(x1, y1, z1, x1, y2, z1, color);
        }
        if (((neighbours & EspBlockNeighbourFlags.LE) != EspBlockNeighbourFlags.LE && (neighbours & EspBlockNeighbourFlags.FO) != EspBlockNeighbourFlags.FO)
                || ((neighbours & EspBlockNeighbourFlags.LE) == EspBlockNeighbourFlags.LE && (neighbours & EspBlockNeighbourFlags.FO) == EspBlockNeighbourFlags.FO && (neighbours & EspBlockNeighbourFlags.FO_LE) != EspBlockNeighbourFlags.FO_LE)) {
            sink.line(x1, y1, z2, x1, y2, z2, color);
        }
        if (((neighbours & EspBlockNeighbourFlags.RI) != EspBlockNeighbourFlags.RI && (neighbours & EspBlockNeighbourFlags.BA) != EspBlockNeighbourFlags.BA)
                || ((neighbours & EspBlockNeighbourFlags.RI) == EspBlockNeighbourFlags.RI && (neighbours & EspBlockNeighbourFlags.BA) == EspBlockNeighbourFlags.BA && (neighbours & EspBlockNeighbourFlags.BA_RI) != EspBlockNeighbourFlags.BA_RI)) {
            sink.line(x2, y1, z1, x2, y2, z1, color);
        }
        if (((neighbours & EspBlockNeighbourFlags.RI) != EspBlockNeighbourFlags.RI && (neighbours & EspBlockNeighbourFlags.FO) != EspBlockNeighbourFlags.FO)
                || ((neighbours & EspBlockNeighbourFlags.RI) == EspBlockNeighbourFlags.RI && (neighbours & EspBlockNeighbourFlags.FO) == EspBlockNeighbourFlags.FO && (neighbours & EspBlockNeighbourFlags.FO_RI) != EspBlockNeighbourFlags.FO_RI)) {
            sink.line(x2, y1, z2, x2, y2, z2, color);
        }

        if (((neighbours & EspBlockNeighbourFlags.BA) != EspBlockNeighbourFlags.BA && (neighbours & EspBlockNeighbourFlags.BO) != EspBlockNeighbourFlags.BO)
                || ((neighbours & EspBlockNeighbourFlags.BA) != EspBlockNeighbourFlags.BA && (neighbours & EspBlockNeighbourFlags.BO_BA) == EspBlockNeighbourFlags.BO_BA)) {
            sink.line(x1, y1, z1, x2, y1, z1, color);
        }
        if (((neighbours & EspBlockNeighbourFlags.FO) != EspBlockNeighbourFlags.FO && (neighbours & EspBlockNeighbourFlags.BO) != EspBlockNeighbourFlags.BO)
                || ((neighbours & EspBlockNeighbourFlags.FO) != EspBlockNeighbourFlags.FO && (neighbours & EspBlockNeighbourFlags.BO_FO) == EspBlockNeighbourFlags.BO_FO)) {
            sink.line(x1, y1, z2, x2, y1, z2, color);
        }
        if (((neighbours & EspBlockNeighbourFlags.BA) != EspBlockNeighbourFlags.BA && (neighbours & EspBlockNeighbourFlags.TO) != EspBlockNeighbourFlags.TO)
                || ((neighbours & EspBlockNeighbourFlags.BA) != EspBlockNeighbourFlags.BA && (neighbours & EspBlockNeighbourFlags.TO_BA) == EspBlockNeighbourFlags.TO_BA)) {
            sink.line(x1, y2, z1, x2, y2, z1, color);
        }
        if (((neighbours & EspBlockNeighbourFlags.FO) != EspBlockNeighbourFlags.FO && (neighbours & EspBlockNeighbourFlags.TO) != EspBlockNeighbourFlags.TO)
                || ((neighbours & EspBlockNeighbourFlags.FO) != EspBlockNeighbourFlags.FO && (neighbours & EspBlockNeighbourFlags.TO_FO) == EspBlockNeighbourFlags.TO_FO)) {
            sink.line(x1, y2, z2, x2, y2, z2, color);
        }

        if (((neighbours & EspBlockNeighbourFlags.LE) != EspBlockNeighbourFlags.LE && (neighbours & EspBlockNeighbourFlags.BO) != EspBlockNeighbourFlags.BO)
                || ((neighbours & EspBlockNeighbourFlags.LE) != EspBlockNeighbourFlags.LE && (neighbours & EspBlockNeighbourFlags.BO_LE) == EspBlockNeighbourFlags.BO_LE)) {
            sink.line(x1, y1, z1, x1, y1, z2, color);
        }
        if (((neighbours & EspBlockNeighbourFlags.RI) != EspBlockNeighbourFlags.RI && (neighbours & EspBlockNeighbourFlags.BO) != EspBlockNeighbourFlags.BO)
                || ((neighbours & EspBlockNeighbourFlags.RI) != EspBlockNeighbourFlags.RI && (neighbours & EspBlockNeighbourFlags.BO_RI) == EspBlockNeighbourFlags.BO_RI)) {
            sink.line(x2, y1, z1, x2, y1, z2, color);
        }
        if (((neighbours & EspBlockNeighbourFlags.LE) != EspBlockNeighbourFlags.LE && (neighbours & EspBlockNeighbourFlags.TO) != EspBlockNeighbourFlags.TO)
                || ((neighbours & EspBlockNeighbourFlags.LE) != EspBlockNeighbourFlags.LE && (neighbours & EspBlockNeighbourFlags.TO_LE) == EspBlockNeighbourFlags.TO_LE)) {
            sink.line(x1, y2, z1, x1, y2, z2, color);
        }
        if (((neighbours & EspBlockNeighbourFlags.RI) != EspBlockNeighbourFlags.RI && (neighbours & EspBlockNeighbourFlags.TO) != EspBlockNeighbourFlags.TO)
                || ((neighbours & EspBlockNeighbourFlags.RI) != EspBlockNeighbourFlags.RI && (neighbours & EspBlockNeighbourFlags.TO_RI) == EspBlockNeighbourFlags.TO_RI)) {
            sink.line(x2, y2, z1, x2, y2, z2, color);
        }
    }

    private static Vec3 screenCenterWorld(Matrix4fc projectionMatrix, Matrix4fc modelViewMatrix, Vec3 cameraPos) {
        Matrix4f inverseProjection = new Matrix4f(projectionMatrix).invert();
        Matrix4f inverseView = new Matrix4f(modelViewMatrix).invert();
        Vector4f center = new Vector4f(0.0F, 0.0F, 0.0F, 1.0F).mul(inverseProjection).mul(inverseView);
        center.div(center.w);
        return new Vec3(cameraPos.x + center.x, cameraPos.y + center.y, cameraPos.z + center.z);
    }

    private static void emitFullBox(LineSink sink, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        sink.line(x1, y1, z1, x1, y2, z1, color);
        sink.line(x1, y1, z2, x1, y2, z2, color);
        sink.line(x2, y1, z1, x2, y2, z1, color);
        sink.line(x2, y1, z2, x2, y2, z2, color);
        sink.line(x1, y1, z1, x2, y1, z1, color);
        sink.line(x1, y1, z2, x2, y1, z2, color);
        sink.line(x1, y2, z1, x2, y2, z1, color);
        sink.line(x1, y2, z2, x2, y2, z2, color);
        sink.line(x1, y1, z1, x1, y1, z2, color);
        sink.line(x2, y1, z1, x2, y1, z2, color);
        sink.line(x1, y2, z1, x1, y2, z2, color);
        sink.line(x2, y2, z1, x2, y2, z2, color);
    }

    private static final class LineSink implements EspBlockTracerRenderer.LineEmitter {
        private final double cameraX;
        private final double cameraY;
        private final double cameraZ;
        private final EspBlockLineMesh mesh;
        private int renderedBlocks;
        private int emittedLines;
        private int skippedLines;

        private LineSink(Minecraft client, EspBlockLineMesh mesh) {
            this.cameraX = client.gameRenderer.mainCamera().position().x;
            this.cameraY = client.gameRenderer.mainCamera().position().y;
            this.cameraZ = client.gameRenderer.mainCamera().position().z;
            this.mesh = mesh;
        }

        private Vec3 cameraPos() {
            return new Vec3(this.cameraX, this.cameraY, this.cameraZ);
        }

        @Override
        public void line(double x1, double y1, double z1, double x2, double y2, double z2, int color) {
            if (this.emittedLines >= MAX_LINES_PER_FRAME) {
                this.skippedLines++;
                return;
            }

            this.mesh.line(x1, y1, z1, x2, y2, z2, color, this.cameraX, this.cameraY, this.cameraZ);
            this.emittedLines++;
        }
    }
}
