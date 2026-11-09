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
public final class UserFixturePreconditionsTest {
  @Test
  @DisplayName("Пользователь отображает друга и входящую заявку")
  void shouldDisplayConfiguredFriendsAndIncomeInvitations(@User(friends = 1, incomeInvitations = 1) TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .clickFriendsTab()
        .checkThatFriendsCount(1)
        .clickIncomeInvitationsTab()
        .checkThatIncomeInvitationsCount(1);
  }
}
