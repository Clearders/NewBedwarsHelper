package org.exmple.newbedwarshelper.client.utils;

public class RateLimiter {
    private final double capacity;
    private final double refillPerSecond;
    private double tokens;
    private long lastRefillNanos;
    private final long minIntervalNanos;
    private long lastConsumeNanos;

    public RateLimiter(double capacity, double refillPerSecond, long minIntervalMs) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.tokens = capacity;
        this.lastRefillNanos = System.nanoTime();
        this.minIntervalNanos = Math.max(0, minIntervalMs) * 1_000_000L;
        this.lastConsumeNanos = 0L;
    }

    private void refill() {
        long now = System.nanoTime();
        double elapsedSeconds = (now - lastRefillNanos) / 1_000_000_000.0;
        if (elapsedSeconds <= 0) {
            return;
        }

        tokens = Math.min(capacity, tokens + elapsedSeconds * refillPerSecond);
        lastRefillNanos = now;
    }

    public synchronized void consume() throws InterruptedException {
        while (true) {
            if (minIntervalNanos > 0 && lastConsumeNanos > 0) {
                long now = System.nanoTime();
                long elapsedNanos = now - lastConsumeNanos;
                if (elapsedNanos < minIntervalNanos) {
                    long waitMillis = Math.max(1, (minIntervalNanos - elapsedNanos) / 1_000_000L);
                    this.wait(waitMillis);
                    continue;
                }
            }

            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                lastConsumeNanos = System.nanoTime();
                return;
            }

            double needed = 1.0 - tokens;
            long waitNanos = (long) Math.ceil((needed / refillPerSecond) * 1_000_000_000.0);
            long waitMillis = Math.max(1, waitNanos / 1_000_000);
            this.wait(waitMillis);
        }
    }
}
