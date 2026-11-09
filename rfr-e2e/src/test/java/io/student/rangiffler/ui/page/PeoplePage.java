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
  private final Locator outcomeInvitationsTab;
  private final Locator searchField;
  private final Locator searchButton;
  private final Locator table;
  private final Locator removeButtons;
  private final Locator acceptButtons;
  private final Locator declineButtons;
  private final Locator cancelButtons;
  private final Locator waitingLabels;
  private final Locator emptyState;

  public PeoplePage(Page page) {
    this.page = page;
    friendsTab = page.getByRole(AriaRole.TAB, new GetByRoleOptions().setName("Friends").setExact(true));
    allPeopleTab = page.getByRole(AriaRole.TAB, new GetByRoleOptions().setName("All People").setExact(true));
    incomeInvitationsTab = page.getByRole(AriaRole.TAB, new GetByRoleOptions().setName("Income invitations").setExact(true));
    outcomeInvitationsTab = page.getByRole(AriaRole.TAB, new GetByRoleOptions().setName("Outcome invitations").setExact(true));
    searchField = page.getByRole(AriaRole.TEXTBOX, new GetByRoleOptions().setName("search people").setExact(true));
    searchButton = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("search").setExact(true));
    table = page.getByRole(AriaRole.TABLE);
    removeButtons = page.getByRole(AriaRole.TABPANEL)
        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove").setExact(true));
    acceptButtons = page.getByRole(AriaRole.TABPANEL)
        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Accept").setExact(true));
    declineButtons = page.getByRole(AriaRole.TABPANEL)
        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Decline").setExact(true));
    cancelButtons = page.getByRole(AriaRole.TABPANEL)
        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Cancel").setExact(true));
    waitingLabels = page.getByRole(AriaRole.TABPANEL).getByText("Waiting...", new Locator.GetByTextOptions().setExact(true));
    emptyState = page.getByText("There are no users yet", new Page.GetByTextOptions().setExact(true));
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

  @Step("Проверить друга: {username}")
  public PeoplePage checkThatFriendIsDisplayed(String username) {
    checkThatUserIsDisplayed(username);
    PlaywrightAssertions.assertThat(acceptButtons).hasCount(0);
    PlaywrightAssertions.assertThat(declineButtons).hasCount(0);
    PlaywrightAssertions.assertThat(cancelButtons).hasCount(0);
    PlaywrightAssertions.assertThat(waitingLabels).hasCount(0);
    return this;
  }

  @Step("Проверить пустое состояние вкладки друзей")
  public PeoplePage checkThatFriendsAreEmpty() {
    PlaywrightAssertions.assertThat(removeButtons).hasCount(0);
    return checkThatEmptyState();
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

  @Step("Проверить входящую заявку от пользователя: {username}")
  public PeoplePage checkThatIncomeInvitationIsDisplayed(String username) {
    checkThatUserIsDisplayed(username);
    PlaywrightAssertions.assertThat(acceptButtons).hasCount(1);
    PlaywrightAssertions.assertThat(declineButtons).hasCount(1);
    PlaywrightAssertions.assertThat(removeButtons).hasCount(0);
    PlaywrightAssertions.assertThat(cancelButtons).hasCount(0);
    PlaywrightAssertions.assertThat(waitingLabels).hasCount(0);
    return this;
  }

  @Step("Проверить пустое состояние вкладки входящих заявок")
  public PeoplePage checkThatIncomeInvitationsAreEmpty() {
    PlaywrightAssertions.assertThat(acceptButtons).hasCount(0);
    PlaywrightAssertions.assertThat(declineButtons).hasCount(0);
    return checkThatEmptyState();
  }

  @Step("Открыть вкладку исходящих заявок")
  public PeoplePage clickOutcomeInvitationsTab() {
    outcomeInvitationsTab.click();
    return this;
  }

  @Step("Проверить исходящую заявку пользователю: {username}")
  public PeoplePage checkThatOutcomeInvitationIsDisplayed(String username) {
    checkThatUserIsDisplayed(username);
    PlaywrightAssertions.assertThat(waitingLabels).hasCount(1);
    PlaywrightAssertions.assertThat(cancelButtons).hasCount(1);
    PlaywrightAssertions.assertThat(removeButtons).hasCount(0);
    PlaywrightAssertions.assertThat(acceptButtons).hasCount(0);
    PlaywrightAssertions.assertThat(declineButtons).hasCount(0);
    return this;
  }

  @Step("Проверить пустое состояние вкладки исходящих заявок")
  public PeoplePage checkThatOutcomeInvitationsAreEmpty() {
    PlaywrightAssertions.assertThat(waitingLabels).hasCount(0);
    PlaywrightAssertions.assertThat(cancelButtons).hasCount(0);
    return checkThatEmptyState();
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

  private PeoplePage checkThatUserIsDisplayed(String username) {
    Locator userRow = table.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(username));
    PlaywrightAssertions.assertThat(userRow).hasCount(1);
    return this;
  }

  private PeoplePage checkThatEmptyState() {
    PlaywrightAssertions.assertThat(emptyState).hasCount(1);
    return this;
  }
}
