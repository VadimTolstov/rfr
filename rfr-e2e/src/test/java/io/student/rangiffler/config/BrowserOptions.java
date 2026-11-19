package io.student.rangiffler.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;

public final class BrowserOptions implements OptionsFactory {
  @Override
  public Options getOptions() {
    return new Options()
        .setBrowserName(TestConfig.browser())
        .setHeadless(TestConfig.headless())
        .setContextOptions(new Browser.NewContextOptions().setLocale("en-US"));
  }
}
