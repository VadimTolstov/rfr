package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public final class CredentialsForm {

  private final Page page;
  private final com.microsoft.playwright.Locator usernameField;
  private final com.microsoft.playwright.Locator passwordField;
  private final com.microsoft.playwright.Locator passwordConfirmationField;

  public CredentialsForm(Page page) {
    this.page = page;
    this.usernameField = page.getByPlaceholder("Type your username");
    this.passwordField = page.getByPlaceholder("Type your password");
    this.passwordConfirmationField = page.getByPlaceholder("Repeat your password");
  }

  @Step("Заполнить поле Username: {username}")
  public CredentialsForm fillUsername(String username) {
    usernameField.fill(username);
    return this;
  }

  @Step("Заполнить поле Password: {password}")
  public CredentialsForm fillPassword(String password) {
    passwordField.fill(password);
    return this;
  }

  @Step("Заполнить поле Confirm password: {passwordConfirmation}")
  public CredentialsForm fillPasswordConfirmation(String passwordConfirmation) {
    passwordConfirmationField.fill(passwordConfirmation);
    return this;
  }
}
