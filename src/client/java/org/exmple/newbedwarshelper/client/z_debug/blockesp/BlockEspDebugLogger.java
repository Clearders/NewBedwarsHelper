package org.exmple.newbedwarshelper.client.z_debug.blockesp;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

/**
 * Temporary Block ESP chunk-cache diagnostics.
 * Remove runtime call sites after the chunk worker behavior is verified.
 */
public final class BlockEspDebugLogger {
    private static final Logger LOGGER = LogUtils.getLogger();

    private BlockEspDebugLogger() {
    }

    public static void loadedChunksSubmitted(int submitted, int cachedChunks, int cachedBlocks) {
        LOGGER.info(
                "[NBH Block ESP Debug] submittedLoadedChunks={}, cachedChunks={}, cachedBlocks={}",
                submitted,
                cachedChunks,
                cachedBlocks
        );
    }

    public static void chunkScanned(ChunkPos pos, int hits, int cachedChunks, int cachedBlocks) {
        LOGGER.info(
                "[NBH Block ESP Debug] chunkScanned=({}, {}), hits={}, cachedChunks={}, cachedBlocks={}",
                pos.x(),
                pos.z(),
                hits,
                cachedChunks,
                cachedBlocks
        );
    }

    public static void chunksTrimmed(int removed, int cachedChunks, int cachedBlocks) {
        LOGGER.info(
                "[NBH Block ESP Debug] chunksTrimmed={}, cachedChunks={}, cachedBlocks={}",
                removed,
                cachedChunks,
                cachedBlocks
        );
    }

    public static void renderedFrame(int blocks, int lines, int skippedLines) {
        if (skippedLines > 0) {
            LOGGER.info("[NBH Block ESP Debug] renderedBlocks={}, emittedLines={}, skippedLines={}", blocks, lines, skippedLines);
        }
    }
}
