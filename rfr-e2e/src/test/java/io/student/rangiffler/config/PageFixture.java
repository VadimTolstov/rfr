package io.student.rangiffler.config;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public class PageFixture implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

  private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
  private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

  @Override
  public void beforeEach(ExtensionContext context) {
    if (PLAYWRIGHT.get() == null) {
      PLAYWRIGHT.set(Playwright.create());
    }
    PAGE.set(PLAYWRIGHT.get().chromium().launch().newPage());
  }

  @Override
  public void afterEach(ExtensionContext context) {
    if (PAGE.get() != null) {
      PAGE.get().close();
    }
    if (PLAYWRIGHT.get() != null) {
      PLAYWRIGHT.get().close();
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.getParameter().getType() == Page.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return PAGE.get();
  }
}
