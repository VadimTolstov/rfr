package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.config.PlaywrightOptionsFactory;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("ui")
@UsePlaywright(PlaywrightOptionsFactory.class)
public final class IncomeInvitationsTabTest {
  @Test
  @DisplayName("Во вкладке входящих заявок показана только ожидающая заявка с кнопками принятия и отклонения")
  void shouldShowOnlyIncomeInvitationWithActions(@User(incomeInvitations = 1) TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .clickIncomeInvitationsTab()
        .checkThatIncomeInvitationsCount(1)
        .checkThatIncomeInvitationIsDisplayed(user.incomeInvitations().getFirst().username());
  }

  @Test
  @DisplayName("Пустая вкладка входящих заявок показывает пустое состояние")
  void shouldShowEmptyState(@User TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .clickIncomeInvitationsTab()
        .checkThatIncomeInvitationsAreEmpty();
  }
}
