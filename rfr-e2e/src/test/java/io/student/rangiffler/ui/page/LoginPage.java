package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.CredentialsForm;

public final class LoginPage {

  private final Page page;
  private final CredentialsForm credentialsForm;
  private final com.microsoft.playwright.Locator loginButton;
  private final com.microsoft.playwright.Locator signInButton;
  private final com.microsoft.playwright.Locator registerLink;

  public LoginPage(Page page) {
    this.page = page;
    this.credentialsForm = new CredentialsForm(page);
    this.loginButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));
    this.signInButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in"));
    this.registerLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign up"));
  }

  @Step("Открыть главную страницу")
  public LoginPage open() {
    page.navigate(TestConfig.FRONTEND_URL);
    page.waitForLoadState();
    return this;
  }

  @Step("Нажать кнопку Login")
  public LoginPage clickLogin() {
    loginButton.click();
    page.waitForLoadState();
    return this;
  }

  @Step("Заполнить форму входа: {username} / {password}")
  public LoginPage fillLoginForm(String username, String password) {
    credentialsForm.fillUsername(username);
    credentialsForm.fillPassword(password);
    return this;
  }

  @Step("Нажать кнопку Sign in")
  public MainPage clickSignIn() {
    signInButton.click();
    page.waitForLoadState();
    return new MainPage(page);
  }

  @Step("Перейти на страницу регистрации")
  public RegistrationPage goToRegister() {
    registerLink.click();
    page.waitForLoadState();
    return new RegistrationPage(page);
  }
}
