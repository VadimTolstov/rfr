package io.student.rangiffler.jupiter.extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Registers one owner of the shared database pools per Jupiter run. */
public final class DatabasePoolsExtension implements BeforeAllCallback {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(DatabasePoolsExtension.class);
    private static final DatabasePoolLifecycle LIFECYCLE = new DatabasePoolLifecycle();

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getRoot().getStore(NAMESPACE)
                .computeIfAbsent(PoolLease.class, key -> LIFECYCLE.lease(), PoolLease.class);
    }
}
