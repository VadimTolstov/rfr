package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import io.student.rangiffler.config.TestConfig;

/** Defines consistent Playwright browser settings for UI tests. */
public final class BrowserOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
        return new Options()
                .setBrowserName(TestConfig.browser())
                .setHeadless(TestConfig.headless())
                .setTestIdAttribute(TestConfig.testIdAttribute())
                .setContextOptions(contextOptions());
    }

    private NewContextOptions contextOptions() {
        return new NewContextOptions()
                .setLocale(TestConfig.browserLocale())
                .setViewportSize(TestConfig.screenWidth(), TestConfig.screenHeight())
                .setScreenSize(TestConfig.screenWidth(), TestConfig.screenHeight());
    }
}
