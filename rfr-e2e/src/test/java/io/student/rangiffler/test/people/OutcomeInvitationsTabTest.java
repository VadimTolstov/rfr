package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.config.PlaywrightOptionsFactory;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UsePlaywright(PlaywrightOptionsFactory.class)
public final class OutcomeInvitationsTabTest {
  @Test
  @DisplayName("Во вкладке исходящих заявок показана только ожидающая заявка с кнопкой отмены")
  void shouldShowOnlyOutcomeInvitationWithCancelAction(@User(outcomeInvitations = 1) TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .clickOutcomeInvitationsTab()
        .checkThatOutcomeInvitationIsDisplayed(user.outcomeInvitations().getFirst().username());
  }

  @Test
  @DisplayName("Пустая вкладка исходящих заявок показывает пустое состояние")
  void shouldShowEmptyState(@User TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .clickOutcomeInvitationsTab()
        .checkThatOutcomeInvitationsAreEmpty();
  }
}
