package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

public final class MainPage {
  public static final String URL = TestConfig.FRONTEND_URL + "/";

  private final Page page;
  private final Locator travelsMap;
  private final Locator profileLink;
  private final Locator peopleLink;

  public MainPage(Page page) {
    this.page = page;
    travelsMap = page.getByRole(AriaRole.HEADING, new GetByRoleOptions().setName("Travels map"));
    profileLink = page.getByRole(AriaRole.BANNER)
        .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Profile").setExact(true));
    peopleLink = page.getByRole(AriaRole.LINK, new GetByRoleOptions().setName("People").setExact(true));
  }

  @Step("Проверить, что карта путешествий отображается")
  public MainPage checkThatTravelsMapVisible() {
    PlaywrightAssertions.assertThat(travelsMap).isVisible();
    return this;
  }

  @Step("Открыть профиль пользователя")
  public ProfilePage clickProfile() {
    profileLink.click();
    return new ProfilePage(page);
  }

  @Step("Открыть список пользователей")
  public PeoplePage clickPeople() {
    peopleLink.click();
    return new PeoplePage(page);
  }
}
