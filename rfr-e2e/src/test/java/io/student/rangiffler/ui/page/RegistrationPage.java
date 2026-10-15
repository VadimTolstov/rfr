package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.ui.UiText;
import io.student.rangiffler.ui.component.CredentialsForm;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class RegistrationPage {

  private final Page page;
  private final CredentialsForm credentialsForm;
  private final com.microsoft.playwright.Locator signUpButton;
  private final com.microsoft.playwright.Locator signInLink;
  private final com.microsoft.playwright.Locator successMessage;

  public RegistrationPage(Page page) {
    this.page = page;
    this.credentialsForm = new CredentialsForm(page);
    this.signUpButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up"));
    this.signInLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign in"));
    this.successMessage = page.getByText(UiText.REGISTRATION_SUCCESS_MESSAGE);
  }

  @Step("Заполнить форму регистрации: {username} / {password} / {passwordConfirmation}")
  public RegistrationPage fillRegistrationForm(String username, String password, String passwordConfirmation) {
    credentialsForm.fillUsername(username);
    credentialsForm.fillPassword(password);
    credentialsForm.fillPasswordConfirmation(passwordConfirmation);
    return this;
  }

  @Step("Нажать кнопку Sign up")
  public RegistrationPage clickSignUp() {
    signUpButton.click();
    return this;
  }

  @Step("Проверить сообщение об успешной регистрации: {expectedText}")
  public RegistrationPage checkThatRegistrationSuccess(String expectedText) {
    assertThat(successMessage).hasText(expectedText);
    return this;
  }

  @Step("Перейти на страницу входа")
  public LoginPage goToLogin() {
    signInLink.click();
    page.waitForLoadState();
    return new LoginPage(page);
  }
}
