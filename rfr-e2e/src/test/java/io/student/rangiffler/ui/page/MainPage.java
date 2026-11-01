package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

public final class MainPage {
  public static final String URL = TestConfig.FRONTEND_URL + "/";

  private final Locator travelsMap;

  public MainPage(Page page) {
    travelsMap = page.getByText("Travels map", new Page.GetByTextOptions().setExact(true));
  }

  @Step("Проверить, что карта путешествий отображается")
  public MainPage checkThatTravelsMapVisible() {
    PlaywrightAssertions.assertThat(travelsMap).isVisible();
    return this;
  }
}
