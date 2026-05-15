package org.exmple.newbedwarshelper.client.enemystatusviewer;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class BedwarsDebugLogger {
    public static final boolean ENABLED = true;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long RENDER_LOG_WINDOW_MS = 1000L;
    private static final int MAX_RENDER_LOGS_PER_WINDOW = 12;

    private static long renderWindowStartedAt;
    private static int renderLogsInWindow;

    private BedwarsDebugLogger() {
    }

    public static void detector(String message) {
        log("detector", message);
    }

    public static void tracker(String message) {
        log("tracker", message);
    }

    public static void invisibility(String message) {
        log("invisibility", message);
    }

    public static void renderer(String message) {
        if (!ENABLED || !canLogRendererMessage()) {
            return;
        }

        log("renderer", message);
    }

    private static void log(String category, String message) {
        if (ENABLED) {
            LOGGER.info("[NBH BedWars Debug] [{}] {}", category, message);
        }
    }

    private static boolean canLogRendererMessage() {
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
