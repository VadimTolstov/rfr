package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.CredentialsForm;

public final class LoginPage {
  public static final String URL = TestConfig.frontUrl() + "/";

  private final Page page;
  private final CredentialsForm credentialsForm;
  private final Locator loginButton;
  private final Locator signInButton;
  private final Locator badCredentialsError;

  public LoginPage(Page page) {
    this.page = page;
    credentialsForm = new CredentialsForm(page);
    loginButton = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Login"));
    signInButton = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Sign in"));
    badCredentialsError = page.locator(".form__error");
  }

  @Step("Открыть главную страницу Rangiffler")
  public LoginPage open() {
    page.navigate(URL);
    return this;
  }

  @Step("Перейти на страницу авторизации")
  public LoginPage clickLogin() {
    loginButton.click();
    return this;
  }

  @Step("Перейти на страницу регистрации")
  public RegistrationPage clickRegister() {
    page.getByRole(AriaRole.LINK, new GetByRoleOptions().setName("Sign up")).click();
    return new RegistrationPage(page);
  }

  @Step("Заполнить форму входа: {username} / {password}")
  public LoginPage fillLoginForm(String username, String password) {
    credentialsForm.fillLoginCredentials(username, password);
    return this;
  }

  @Step("Войти в Rangiffler")
  public MainPage clickSignIn() {
    signInButton.click();
    return new MainPage(page);
  }

  @Step("Отправить форму входа с неверными данными")
  public LoginPage clickSignInExpectingBadCredentials() {
    signInButton.click();
    return this;
  }

  @Step("Проверить сообщение об ошибке входа: Bad credentials")
  public LoginPage checkThatBadCredentials() {
    PlaywrightAssertions.assertThat(badCredentialsError).hasText("Bad credentials");
    return this;
  }
}
