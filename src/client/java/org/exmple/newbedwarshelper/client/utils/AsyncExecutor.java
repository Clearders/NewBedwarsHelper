package org.exmple.newbedwarshelper.client.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AsyncExecutor {
    private static final ExecutorService IO_EXECUTOR = createExecutor();

    private AsyncExecutor() {
    }

    private static ExecutorService createExecutor() {
        return Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName("NewBedwarsHelper-IO");
            return thread;
        });
    }

    public static ExecutorService getExecutor() {
        return IO_EXECUTOR;
    }

    public static void shutdown() {
        if (!IO_EXECUTOR.isShutdown()) {
            IO_EXECUTOR.shutdown();
        }
    }
}
