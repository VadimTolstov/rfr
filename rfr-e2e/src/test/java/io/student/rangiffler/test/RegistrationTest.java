package io.student.rangiffler.test;

import com.microsoft.playwright.Page;
import io.student.rangiffler.config.PageFixture;
import io.student.rangiffler.ui.page.LoginPage;
import io.student.rangiffler.ui.page.MainPage;
import io.student.rangiffler.ui.page.RegistrationPage;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PageFixture.class)
public class RegistrationTest {
  @Test
  @DisplayName("Регистрация пользователя и успешная авторизация")
  void shouldRegisterUserAndAuthorizeSuccessfully(Page page) {
    String username = RandomDataUtils.randomUsername();
    String password = RandomDataUtils.randomPassword();

    new LoginPage(page)
        .open()
        .clickLogin()
        .clickRegister()
        .fillRegistrationForm(username, password, password)
        .clickSignUp()
        .checkThatRegistrationSucceeded()
        .clickSignIn()
        .clickLogin()
        .fillLoginForm(username, password)
        .clickSignIn()
        .checkThatTravelsMapVisible();
  }
}
