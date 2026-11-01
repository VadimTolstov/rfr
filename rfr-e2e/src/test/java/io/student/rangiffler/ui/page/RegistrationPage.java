package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Step;
import com.microsoft.playwright.options.AriaRole;
import io.student.rangiffler.ui.component.CredentialsForm;

public final class RegistrationPage {
  private final Page page;
  private final CredentialsForm credentialsForm;
  private final Locator signUpButton;
  private final Locator registrationSuccessMessage;
  private final Locator signInLink;

  public RegistrationPage(Page page) {
    this.page = page;
    credentialsForm = new CredentialsForm(page);
    signUpButton = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Sign up"));
    registrationSuccessMessage = page.getByText("Congratulations! You've registered!", new Page.GetByTextOptions().setExact(true));
    signInLink = page.getByRole(AriaRole.LINK, new GetByRoleOptions().setName("Sign in"));
  }

  @Step("Заполнить форму регистрации: {username} / {password} / {passwordConfirmation}")
  public RegistrationPage fillRegistrationForm(String username, String password, String passwordConfirmation) {
    credentialsForm.fillRegistrationCredentials(username, password, passwordConfirmation);
    return this;
  }

  @Step("Отправить форму регистрации")
  public RegistrationPage clickSignUp() {
    signUpButton.click();
    return this;
  }

  @Step("Проверить успешную регистрацию")
  public RegistrationPage checkThatRegistrationSucceeded() {
    PlaywrightAssertions.assertThat(registrationSuccessMessage).isVisible();
    return this;
  }

  @Step("Перейти ко входу после регистрации")
  public LoginPage clickSignIn() {
    signInLink.click();
    return new LoginPage(page);
  }
}
