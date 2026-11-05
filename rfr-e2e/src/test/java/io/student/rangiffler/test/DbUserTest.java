package io.student.rangiffler.test;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.config.PlaywrightOptionsFactory;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.ui.page.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UsePlaywright(PlaywrightOptionsFactory.class)
public final class DbUserTest {
  @Test
  @DisplayName("Успешный вход в систему с пользователем из БД")
  void shouldLoginWithDbUser(@User TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .checkThatTravelsMapVisible();
  }
}
