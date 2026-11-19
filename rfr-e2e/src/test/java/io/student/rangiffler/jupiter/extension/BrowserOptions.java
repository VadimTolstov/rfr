package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import io.student.rangiffler.config.TestConfig;

/** Defines consistent Playwright browser settings for UI tests. */
public final class BrowserOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
        int width = TestConfig.screenWidth();
        int height = TestConfig.screenHeight();
        return new Options()
                .setBrowserName(TestConfig.browser())
                .setHeadless(TestConfig.headless())
                .setTestIdAttribute(TestConfig.testIdAttribute())
                .setContextOptions(new Browser.NewContextOptions()
                        .setLocale(TestConfig.browserLocale())
                        .setViewportSize(width, height)
                        .setScreenSize(width, height));
    }
}
