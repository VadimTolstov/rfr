package io.student.rangiffler.test.auth;

import com.microsoft.playwright.Page;
import io.student.rangiffler.config.PageFixture;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.extension.UserExtension;
import io.student.rangiffler.ui.page.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

@ExtendWith(PageFixture.class)
public final class LoginNegativeTest {
  @RegisterExtension
  static final UserExtension USER_EXTENSION = new UserExtension();

  @Test
  @DisplayName("Неверный пароль отображает сообщение Bad credentials")
  void shouldShowBadCredentialsForIncorrectPassword(@User TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password() + "_invalid")
        .clickSignInExpectingBadCredentials()
        .checkThatBadCredentials();
  }

  @Test
  @DisplayName("Несуществующий пользователь отображает сообщение Bad credentials")
  void shouldShowBadCredentialsForUnknownUsername(@User TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username() + "_missing", user.password())
        .clickSignInExpectingBadCredentials()
        .checkThatBadCredentials();
  }
}
