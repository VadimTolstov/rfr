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
public final class FriendsTabTest {
  @Test
  @DisplayName("Во вкладке друзей показана только существующая дружба с кнопкой удаления")
  void shouldShowOnlyFriendWithRemoveAction(@User(friends = 1) TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .clickFriendsTab()
        .checkThatFriendsCount(1)
        .checkThatFriendIsDisplayed(user.friends().getFirst().username());
  }

  @Test
  @DisplayName("Пустая вкладка друзей показывает пустое состояние")
  void shouldShowEmptyState(@User TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .clickFriendsTab()
        .checkThatFriendsAreEmpty();
  }
}
