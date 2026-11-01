package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public final class CredentialsForm {
  private final Locator usernameInput;
  private final Locator passwordInput;
  private final Locator passwordConfirmationInput;

  public CredentialsForm(Page page) {
    usernameInput = page.getByPlaceholder("Type your username");
    passwordInput = page.getByPlaceholder("Type your password");
    passwordConfirmationInput = page.getByPlaceholder("Repeat your password");
  }

  @Step("Заполнить форму входа: {username} / {password}")
  public CredentialsForm fillLoginCredentials(String username, String password) {
    usernameInput.fill(username);
    passwordInput.fill(password);
    return this;
  }

  @Step("Заполнить форму регистрации: {username} / {password} / {passwordConfirmation}")
  public CredentialsForm fillRegistrationCredentials(String username, String password, String passwordConfirmation) {
    usernameInput.fill(username);
    passwordInput.fill(password);
    passwordConfirmationInput.fill(passwordConfirmation);
    return this;
  }
}
