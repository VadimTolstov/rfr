package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.PeopleTable;

public final class PeoplePage {
  public static final String URL = TestConfig.frontUrl() + "/people";

  private final Locator friendsTab;
  private final Locator allPeopleTab;
  private final Locator incomeInvitationsTab;
  private final Locator outcomeInvitationsTab;
  private final PeopleTable table;

  public PeoplePage(Page page) {
    friendsTab = page.getByRole(AriaRole.TAB, new GetByRoleOptions().setName("Frie").setExact(true));
    allPeopleTab = page.getByRole(AriaRole.TAB, new GetByRoleOptions().setName("All People").setExact(true));
    incomeInvitationsTab = page.getByRole(AriaRole.TAB, new GetByRoleOptions().setName("Income invitations").setExact(true));
    outcomeInvitationsTab = page.getByRole(AriaRole.TAB, new GetByRoleOptions().setName("Outcome invitations").setExact(true));
    table = new PeopleTable(page);
  }

  @Step("Открыть вкладку друзей")
  public PeoplePage clickFriendsTab() {
    friendsTab.click();
    return this;
  }

  @Step("Проверить количество друзей: {count}")
  public PeoplePage checkThatFriendsCount(int count) {
    table.checkThatFriendsCount(count);
    return this;
  }

  @Step("Проверить друга: {username}")
  public PeoplePage checkThatFriendIsDisplayed(String username) {
    table.checkThatFriendIsDisplayed(username);
    return this;
  }

  @Step("Проверить пустое состояние вкладки друзей")
  public PeoplePage checkThatFriendsAreEmpty() {
    table.checkThatEmpty();
    return this;
  }

  @Step("Открыть вкладку входящих заявок")
  public PeoplePage clickIncomeInvitationsTab() {
    incomeInvitationsTab.click();
    return this;
  }

  @Step("Проверить количество входящих заявок: {count}")
  public PeoplePage checkThatIncomeInvitationsCount(int count) {
    table.checkThatIncomeInvitationsCount(count);
    return this;
  }

  @Step("Проверить входящую заявку от пользователя: {username}")
  public PeoplePage checkThatIncomeInvitationIsDisplayed(String username) {
    table.checkThatIncomeInvitationIsDisplayed(username);
    return this;
  }

  @Step("Проверить пустое состояние вкладки входящих заявок")
  public PeoplePage checkThatIncomeInvitationsAreEmpty() {
    table.checkThatEmpty();
    return this;
  }

  @Step("Открыть вкладку исходящих заявок")
  public PeoplePage clickOutcomeInvitationsTab() {
    outcomeInvitationsTab.click();
    return this;
  }

  @Step("Проверить исходящую заявку пользователю: {username}")
  public PeoplePage checkThatOutcomeInvitationIsDisplayed(String username) {
    table.checkThatOutcomeInvitationIsDisplayed(username);
    return this;
  }

  @Step("Проверить пустое состояние вкладки исходящих заявок")
  public PeoplePage checkThatOutcomeInvitationsAreEmpty() {
    table.checkThatEmpty();
    return this;
  }

  @Step("Открыть вкладку всех пользователей")
  public PeoplePage clickAllPeopleTab() {
    allPeopleTab.click();
    return this;
  }

  @Step("Найти пользователя: {username}")
  public PeoplePage searchPeople(String username) {
    table.searchPeople(username);
    return this;
  }

  @Step("Отправить заявку пользователю: {username}")
  public PeoplePage clickAddUser(String username) {
    table.clickAddUser(username);
    return this;
  }

  @Step("Проверить, что заявка отправлена пользователю: {username}")
  public PeoplePage checkThatInvitationWasSent(String username) {
    table.checkThatInvitationWasSent(username);
    return this;
  }
}
