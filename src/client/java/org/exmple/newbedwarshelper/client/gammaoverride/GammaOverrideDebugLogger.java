package org.exmple.newbedwarshelper.client.gammaoverride;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

/**
 * Temporary Gamma Override debug logger for shader compatibility testing.
 * Deprecate or remove after the Iris shader path is understood.
 */
public final class GammaOverrideDebugLogger {
    public static final boolean ENABLED = true;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long RENDER_LOG_WINDOW_MS = 1000L;
    private static final int MAX_RENDER_LOGS_PER_WINDOW = 4;

    private static long renderWindowStartedAt;
    private static int renderLogsInWindow;

    private GammaOverrideDebugLogger() {
    }

    public static void lightmap(LightmapRenderState state, float brightnessBefore, float nightVisionBefore) {
        if (!ENABLED || !canLogRenderMessage()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Entity cameraEntity = minecraft.getCameraEntity();
        Entity mainCameraEntity = minecraft.gameRenderer.mainCamera().entity();

        LOGGER.info(
                "[NBH Gamma Override Debug] [lightmap] enabled={} mode={} irisLoaded={} shaderPackInUse={} player={} cameraEntity={} mainCameraEntity={} brightness={}->{} nightVision={}->{} needsUpdate={}",
                GammaOverrideManager.isEnabled(),
                GammaOverrideManager.getMode(),
                GammaOverrideIrisCompat.isIrisLoaded(),
                GammaOverrideIrisCompat.isShaderPackInUse(),
                describeEntity(minecraft.player),
                describeEntity(cameraEntity),
                describeEntity(mainCameraEntity),
                brightnessBefore,
                state.brightness,
                nightVisionBefore,
                state.nightVisionEffectIntensity,
                state.needsUpdate
        );
    }

    private static String describeEntity(Entity entity) {
        if (entity == null) {
            return "null";
        }

        return entity.getType().toShortString() + "@" + Integer.toHexString(System.identityHashCode(entity));
    }

    private static boolean canLogRenderMessage() {
        long now = System.currentTimeMillis();
        if (now - renderWindowStartedAt > RENDER_LOG_WINDOW_MS) {
            renderWindowStartedAt = now;
            renderLogsInWindow = 0;
        }

        if (renderLogsInWindow >= MAX_RENDER_LOGS_PER_WINDOW) {
            return false;
        }

        renderLogsInWindow++;
        return true;
    }
}
