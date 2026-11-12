package io.student.rangiffler.jupiter.extension;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Bounds the whole wait, including DNS and response bodies, by a monotonic deadline. */
final class StandReadiness {
    private final List<StandEndpoint> endpoints;
    private final Duration timeout;

    StandReadiness(List<StandEndpoint> endpoints, Duration timeout) {
        this.endpoints = List.copyOf(endpoints);
        this.timeout = timeout;
    }

    void await() {
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("Stand timeout must be positive, got " + timeout);
        }
        long origin = System.nanoTime();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<StandEndpoint> pending = new ArrayList<>(endpoints);
        System.getLogger("io.student.rangiffler.stand").log(System.Logger.Level.INFO,
                "Waiting up to {0} seconds for the stand", timeout.toSeconds());
        try {
            do {
                for (var iterator = pending.iterator(); iterator.hasNext();) {
                    StandEndpoint endpoint = iterator.next();
                    long budget = timeout.toNanos() - (System.nanoTime() - origin);
                    if (budget > 0 && readiness(executor, endpoint,
                            Duration.ofNanos(Math.min(budget, TimeUnit.SECONDS.toNanos(5))))) {
                        iterator.remove();
                    }
                }
                if (pending.isEmpty()) {
                    return;
                }
                long budget = timeout.toNanos() - (System.nanoTime() - origin);
                if (budget > 0) {
                    TimeUnit.NANOSECONDS.sleep(Math.min(budget, TimeUnit.MILLISECONDS.toNanos(200)));
                }
            } while (System.nanoTime() - origin < timeout.toNanos());
            throw new IllegalStateException("Stand did not become ready within " + timeout.toSeconds()
                    + " seconds; unavailable services: "
                    + String.join(", ", pending.stream().map(StandEndpoint::instruction).toList()));
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Stand readiness wait was interrupted", failure);
        } finally {
            // Closing this executor would wait for DNS or an unresponsive response body after the deadline.
            executor.shutdownNow();
        }
    }

    private boolean readiness(ExecutorService executor, StandEndpoint endpoint, Duration limit)
            throws InterruptedException {
        Future<Boolean> attempt = executor.submit(() -> endpoint.readiness(limit));
        try {
            return attempt.get(limit.toNanos(), TimeUnit.NANOSECONDS);
        } catch (TimeoutException failure) {
            return false;
        } catch (ExecutionException failure) {
            throw new IllegalStateException("Stand probe failed for " + endpoint.instruction(), failure.getCause());
        } finally {
            attempt.cancel(true);
        }
    }
}
