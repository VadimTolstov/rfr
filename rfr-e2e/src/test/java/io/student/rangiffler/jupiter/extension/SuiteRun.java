package io.student.rangiffler.jupiter.extension;

import org.junit.jupiter.api.extension.ExtensionContext;

/** Keeps the preparation outcome for all classes in the same Jupiter run. */
final class SuiteRun implements AutoCloseable {
    private final SuiteExtension extension;
    private final ExtensionContext context;
    private boolean started;
    private Throwable preparation;

    SuiteRun(SuiteExtension extension, ExtensionContext context) {
        this.extension = extension;
        this.context = context;
    }

    synchronized void start() {
        if (!started) {
            started = true;
            try {
                extension.beforeSuite(context);
            } catch (RuntimeException | Error failure) {
                preparation = failure;
            }
        }
        if (preparation instanceof RuntimeException failure) {
            throw failure;
        }
        if (preparation instanceof Error failure) {
            throw failure;
        }
    }

    @Override
    public synchronized void close() {
        try {
            extension.afterSuite(context);
        } catch (RuntimeException | Error failure) {
            if (preparation != null && preparation != failure) {
                failure.addSuppressed(preparation);
            }
            throw failure;
        }
    }
}
