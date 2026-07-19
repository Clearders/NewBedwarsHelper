package org.exmple.newbedwarshelper.client.esp.block.render;

import net.minecraft.world.level.chunk.LevelChunk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class EspBlockScanWorker {
    private final EspBlockChunkCache cache;
    private final AtomicInteger generation = new AtomicInteger();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "NBH Block ESP Scanner");
        thread.setDaemon(true);
        return thread;
    });

    public EspBlockScanWorker(EspBlockChunkCache cache) {
        this.cache = cache;
    }

    public void submit(LevelChunk chunk) {
        int submittedGeneration = this.generation.get();
        this.executor.submit(() -> {
            EspBlockChunkScanner.ScanResult result = EspBlockChunkScanner.scan(chunk);
            if (submittedGeneration != this.generation.get()) {
                return;
            }

            this.cache.replaceChunk(result.pos(), result.entries());
        });
    }

    public void invalidatePendingResults() {
        this.generation.incrementAndGet();
    }

    public void shutdown() {
        this.executor.shutdownNow();
    }
}
