package io.student.rangiffler.test;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.config.PlaywrightOptionsFactory;
import io.student.rangiffler.ui.page.LoginPage;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UsePlaywright(PlaywrightOptionsFactory.class)
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
