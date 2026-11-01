package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.CredentialsForm;

public final class LoginPage {
  public static final String URL = TestConfig.FRONTEND_URL + "/";

  private final Page page;
  private final CredentialsForm credentialsForm;
  private final Locator loginButton;
  private final Locator signInButton;

  public LoginPage(Page page) {
    this.page = page;
    credentialsForm = new CredentialsForm(page);
    loginButton = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Login"));
    signInButton = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Sign in"));
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
}
