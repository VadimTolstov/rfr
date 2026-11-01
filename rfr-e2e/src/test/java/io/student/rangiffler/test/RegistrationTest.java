package io.student.rangiffler.test;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.qameta.allure.Allure;
import io.student.rangiffler.config.PageFixture;
import io.student.rangiffler.ui.page.LoginPage;
import io.student.rangiffler.ui.page.MainPage;
import io.student.rangiffler.ui.page.RegistrationPage;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PageFixture.class)
public class RegistrationTest {
  @Test
  void shouldRegisterUserAndAuthorizeSuccessfully(Page page) {
    String username = "ui" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    String password = "Password123";

    RegistrationPage registrationPage = new RegistrationPage(page);
    Allure.step("Открыть форму регистрации", registrationPage::open);
    Allure.step("Зарегистрировать нового пользователя", () -> registrationPage
        .fillRegistrationForm(username, password, password)
        .clickSignUp()
        .checkThatRegistrationSucceeded());
    Allure.step("Войти с созданными учётными данными", () -> {
      LoginPage loginPage = registrationPage.clickSignIn();
      loginPage.clickLogin();
      loginPage.fillLoginForm(username, password);
      MainPage mainPage = loginPage.clickSignIn();
      mainPage.checkThatTravelsMapVisible();
    });
  }
}
