package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;
import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BrowserTraceExtension implements BeforeEachCallback, AfterTestExecutionCallback {

    @Override
    public void beforeEach(@NonNull ExtensionContext context) throws Exception {
        invokeWithBrowserContext(context, "tracing");
    }

    private void tracing(BrowserContext browserContext) {
        browserContext.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        boolean isEmptyException = context.getExecutionException().isEmpty();
        if (isEmptyException) {
            invokeWithBrowserContext(context, "stopTracing");
        } else {
            invokeWithBrowserContext(context, "saveTracing");
        }
    }

    private void stopTracing(BrowserContext browserContext) {
        browserContext.tracing().stop();
    }

    private void saveTracing(BrowserContext browserContext) throws IOException {
        Path file = File.createTempFile("playwright-trace-", ".zip").toPath();
        try {
            browserContext.tracing().stop(new Tracing.StopOptions().setPath(file));
            try (InputStream inputStream = Files.newInputStream(file)) {
                Allure.attachment("Playwright trace", "application/zip", inputStream, AttachmentOptions.withFileExtension("zip"));
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }


    private void invokeWithBrowserContext(ExtensionContext context, String tracing) throws NoSuchMethodException {
        context.getExecutableInvoker().invoke(
                BrowserTraceExtension.class.getDeclaredMethod(tracing, BrowserContext.class), this
        );
    }
}
