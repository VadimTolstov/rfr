package io.student.rangiffler.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;

public final class PlaywrightOptionsFactory implements OptionsFactory {
  @Override
  public Options getOptions() {
    return new Options()
        .setHeadless(true)
        .setContextOptions(new Browser.NewContextOptions().setLocale("en-US"));
  }
}
