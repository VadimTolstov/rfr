package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class MainPage {

  private final Page page;
  private final com.microsoft.playwright.Locator travelsMapHeading;

  public MainPage(Page page) {
    this.page = page;
    this.travelsMapHeading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Travels map"));
  }

  @Step("Проверить заголовок страницы Travels map")
  public MainPage checkThatTravelsMapVisible() {
    assertThat(travelsMapHeading).isVisible();
    return this;
  }
}
