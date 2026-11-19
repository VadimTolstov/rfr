package io.student.rangiffler.test;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.config.PlaywrightOptionsFactory;
import io.student.rangiffler.data.TearDown;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.db.ApiProfile;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.ui.page.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UsePlaywright(PlaywrightOptionsFactory.class)
public class RegistrationTest {
  @Test
  @DisplayName("Регистрация пользователя и успешная авторизация")
  void shouldRegisterUserAndAuthorizeSuccessfully(Page page) {
    TestUser user = TestUser.unique();
    try {
      new LoginPage(page)
          .open()
          .clickLogin()
          .clickRegister()
          .fillRegistrationForm(user.username(), user.password(), user.password())
          .clickSignUp()
          .checkThatRegistrationSucceeded()
          .clickSignIn()
          .clickLogin()
          .fillLoginForm(user.username(), user.password())
          .clickSignIn()
          .checkThatTravelsMapVisible();
    } finally {
      new TearDown()
          .with(new ApiProfile(user.username())::delete)
          .with(new AuthAccount(user.username(), user.password())::delete)
          .close();
      assertFalse(new ApiProfile(user.username()).exists());
      assertFalse(new AuthAccount(user.username(), user.password()).exists());
    }
  }
}
