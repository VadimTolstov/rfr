package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.BrowserContext;
import io.student.rangiffler.config.TestConfig;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Applies separate action and navigation timeouts to the current Playwright context. */
public final class BrowserTimeoutExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) throws NoSuchMethodException {
        // The invoker lets Playwright resolve and own the same context as the test's Page.
        context.getExecutableInvoker().invoke(
                BrowserTimeoutExtension.class.getDeclaredMethod("configure", BrowserContext.class), this
        );
    }

    private void configure(BrowserContext browser) {
        browser.setDefaultTimeout(TestConfig.actionTimeoutMs());
        browser.setDefaultNavigationTimeout(TestConfig.navigationTimeoutMs());
    }
}
