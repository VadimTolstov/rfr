package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.FilterOptions;
import com.microsoft.playwright.Locator.GetByRoleOptions;
import com.microsoft.playwright.Locator.GetByTextOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.ui.component.PeopleRow.Action;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Owns the active people table, its search and its rows.
 */
public final class PeopleTable {
    private final Page page;
    private final Locator panel;

    public PeopleTable(Page page) {
        this.page = page;
        panel = page.getByRole(AriaRole.TABPANEL);
    }

    @Step("Проверить количество действий Remove в таблице: {count}")
    public PeopleTable checkThatFriendsCount(int count) {
        assertThat(Action.REMOVE.button(rows())).hasCount(count);
        return this;
    }

    @Step("Проверить количество действий Accept в таблице: {count}")
    public PeopleTable checkThatIncomeInvitationsCount(int count) {
        assertThat(Action.ACCEPT.button(rows())).hasCount(count);
        return this;
    }

    @Step("Проверить единственную строку друга: {username}")
    public PeopleTable checkThatFriendIsDisplayed(String username) {
        assertThat(rows()).hasCount(1);
        row(username).checkFriend();
        return this;
    }

    @Step("Проверить единственную строку входящей заявки: {username}")
    public PeopleTable checkThatIncomeInvitationIsDisplayed(String username) {
        assertThat(rows()).hasCount(1);
        row(username).checkIncomeInvitation();
        return this;
    }

    @Step("Проверить единственную строку исходящей заявки: {username}")
    public PeopleTable checkThatOutcomeInvitationIsDisplayed(String username) {
        assertThat(rows()).hasCount(1);
        row(username).checkOutcomeInvitation();
        return this;
    }

    @Step("Проверить, что таблица пуста")
    public PeopleTable checkThatEmpty() {
        assertThat(panel.getByText("There are no users yet", new GetByTextOptions().setExact(true))).isVisible();
        assertThat(rows()).hasCount(0);
        return this;
    }

    @Step("Найти в таблице пользователя: {username}")
    public PeopleTable searchPeople(String username) {
        panel.getByRole(AriaRole.TEXTBOX, new GetByRoleOptions().setName("search people").setExact(true)).fill(username);
        page.waitForResponse(response -> isGraphQlResponse(response, "GetPeople"),
                () -> panel.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("search").setExact(true)).click()
        );
        return this;
    }

    @Step("Нажать Add в строке пользователя: {username}")
    public PeopleTable clickAddUser(String username) {
        page.waitForResponse(response -> isGraphQlResponse(response, "FriendshipAction"),
                row(username)::clickAdd
        );
        return this;
    }

    @Step("Проверить кнопку Cancel в строке пользователя: {username}")
    public PeopleTable checkThatInvitationWasSent(String username) {
        row(username).checkInvitationSent();
        return this;
    }

    private PeopleRow row(String username) {
        return new PeopleRow(rows().filter(new FilterOptions().setHas(
                page.getByText(username, new Page.GetByTextOptions().setExact(true))
        )));
    }

    private Locator rows() {
        return panel.getByTestId("people-row");
    }

    private boolean isGraphQlResponse(Response response, String operation) {
        String body = response.request().postData();
        return response.url().contains("/graphql") && body != null && body.contains(operation);
    }
}
