package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.data.TearDown;
import io.student.rangiffler.data.db.Databases;
import java.util.List;

/** Keeps shared pools alive until every Jupiter root has released its owner. */
final class DatabasePoolLifecycle {
    private int owners;

    synchronized PoolLease lease() {
        owners++;
        return new PoolLease(this);
    }

    synchronized void release() {
        if (--owners == 0) {
            new TearDown().withEach(List.of(Databases.values()), Databases::close).close();
        }
    }
}
