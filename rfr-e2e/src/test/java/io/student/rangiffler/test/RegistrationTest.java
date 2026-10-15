package io.student.rangiffler.test;

import com.microsoft.playwright.Page;
import io.student.rangiffler.config.PageFixture;
import io.student.rangiffler.ui.UiText;
import io.student.rangiffler.ui.page.LoginPage;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RegistrationTest {

  @RegisterExtension
  static PageFixture pageFixture = new PageFixture();

  @Test
  void shouldRegisterUserAndAuthorizeSuccessfully(Page page) {
    String username = RandomDataUtils.randomUsername();
    String password = RandomDataUtils.randomPassword();

    new LoginPage(page)
      .open()
      .clickLogin()
      .goToRegister()
      .fillRegistrationForm(username, password, password)
      .clickSignUp()
      .checkThatRegistrationSuccess(UiText.REGISTRATION_SUCCESS_MESSAGE)
      .goToLogin()
      .clickLogin()
      .fillLoginForm(username, password)
      .clickSignIn()
      .checkThatTravelsMapVisible();
  }
}
