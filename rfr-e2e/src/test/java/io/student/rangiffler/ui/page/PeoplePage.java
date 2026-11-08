package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

public final class PeoplePage {
  public static final String URL = TestConfig.FRONTEND_URL + "/people";

  private final Page page;
  private final Locator friendsTab;
  private final Locator allPeopleTab;
  private final Locator incomeInvitationsTab;
  private final Locator searchField;
  private final Locator searchButton;
  private final Locator table;
  private final Locator removeButtons;
  private final Locator acceptButtons;

  public PeoplePage(Page page) {
    this.page = page;
    friendsTab = page.getByRole(AriaRole.TAB, new GetByRoleOptions().setName("Friends").setExact(true));
    allPeopleTab = page.getByRole(AriaRole.TAB, new GetByRoleOptions().setName("All People").setExact(true));
    incomeInvitationsTab = page.getByRole(AriaRole.TAB, new GetByRoleOptions().setName("Income invitations").setExact(true));
    searchField = page.getByRole(AriaRole.TEXTBOX, new GetByRoleOptions().setName("search people").setExact(true));
    searchButton = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("search").setExact(true));
    table = page.getByRole(AriaRole.TABLE);
    removeButtons = page.getByRole(AriaRole.TABPANEL)
        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove").setExact(true));
    acceptButtons = page.getByRole(AriaRole.TABPANEL)
        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Accept").setExact(true));
  }

  @Step("Открыть вкладку друзей")
  public PeoplePage clickFriendsTab() {
    friendsTab.click();
    return this;
  }

  @Step("Проверить количество друзей: {count}")
  public PeoplePage checkThatFriendsCount(int count) {
    PlaywrightAssertions.assertThat(removeButtons).hasCount(count);
    return this;
  }

  @Step("Открыть вкладку входящих заявок")
  public PeoplePage clickIncomeInvitationsTab() {
    incomeInvitationsTab.click();
    return this;
  }

  @Step("Проверить количество входящих заявок: {count}")
  public PeoplePage checkThatIncomeInvitationsCount(int count) {
    PlaywrightAssertions.assertThat(acceptButtons).hasCount(count);
    return this;
  }

  @Step("Открыть вкладку всех пользователей")
  public PeoplePage clickAllPeopleTab() {
    allPeopleTab.click();
    return this;
  }

  @Step("Найти пользователя: {username}")
  public PeoplePage searchPeople(String username) {
    searchField.fill(username);
    page.waitForResponse(response -> isGraphQlResponse(response, "GetPeople"), searchButton::click);
    return this;
  }

  @Step("Отправить заявку пользователю: {username}")
  public PeoplePage clickAddUser(String username) {
    Locator userRow = table.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(username));
    page.waitForResponse(response -> isGraphQlResponse(response, "FriendshipAction"),
        () -> userRow.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add").setExact(true)).click());
    return this;
  }

  @Step("Проверить, что заявка отправлена пользователю: {username}")
  public PeoplePage checkThatInvitationWasSent(String username) {
    Locator userRow = table.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(username));
    PlaywrightAssertions.assertThat(userRow.getByRole(AriaRole.BUTTON,
        new Locator.GetByRoleOptions().setName("Cancel").setExact(true))).hasCount(1);
    return this;
  }

  private boolean isGraphQlResponse(Response response, String operation) {
    String body = response.request().postData();
    return response.url().contains("/graphql") && body != null && body.contains(operation);
  }
}
