package io.student.rangiffler.test;

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
public final class MainPageTest {
  @RegisterExtension
  static final UserExtension USER_EXTENSION = new UserExtension();

  @Test
  @DisplayName("Успешное отображение главной страницы после входа")
  void shouldOpenMainPageAfterLogin(@User TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .checkThatTravelsMapVisible();
  }
}
