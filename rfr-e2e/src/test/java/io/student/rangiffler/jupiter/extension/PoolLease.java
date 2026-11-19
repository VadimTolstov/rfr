package io.student.rangiffler.jupiter.extension;

import java.util.concurrent.atomic.AtomicBoolean;

/** Releases a Jupiter root's ownership at most once when its Store closes. */
final class PoolLease implements AutoCloseable {
    private final DatabasePoolLifecycle lifecycle;
    private final AtomicBoolean closed = new AtomicBoolean();

    PoolLease(DatabasePoolLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            lifecycle.release();
        }
    }
}
