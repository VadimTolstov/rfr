package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByLabelOptions;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.regex.Pattern;

public final class ProfilePage {
  public static final String URL = TestConfig.FRONTEND_URL + "/profile";

  private final Page page;
  private final Locator firstNameInput;
  private final Locator surnameInput;
  private final Locator locationSelect;
  private final Locator avatarInput;
  private final Locator avatarImage;
  private final Locator saveButton;
  private final Locator resetButton;

  public ProfilePage(Page page) {
    this.page = page;
    firstNameInput = page.getByLabel("First Name", new GetByLabelOptions().setExact(true));
    surnameInput = page.getByLabel("Surname", new GetByLabelOptions().setExact(true));
    locationSelect = page.getByRole(AriaRole.COMBOBOX, new GetByRoleOptions().setName("Location").setExact(true));
    avatarInput = page.getByLabel("Update avatar", new GetByLabelOptions().setExact(true));
    avatarImage = page.getByRole(AriaRole.MAIN).getByRole(AriaRole.IMG,
        new Locator.GetByRoleOptions().setName("").setExact(true));
    saveButton = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Save").setExact(true));
    resetButton = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Reset").setExact(true));
  }

  @Step("Открыть профиль пользователя")
  public ProfilePage open() {
    page.navigate(URL);
    return this;
  }

  @Step("Заполнить профиль: {firstName} / {surname}")
  public ProfilePage fillProfileForm(String firstName, String surname) {
    firstNameInput.fill(firstName);
    surnameInput.fill(surname);
    return this;
  }

  @Step("Выбрать страну профиля: {countryName}")
  public ProfilePage selectLocation(String countryName) {
    locationSelect.click();
    Locator options = page.getByRole(AriaRole.OPTION);
    Locator countryImage = page.getByRole(AriaRole.IMG,
        new GetByRoleOptions().setName(countryName).setExact(true));
    options.filter(new Locator.FilterOptions().setHas(countryImage)).click();
    return this;
  }

  @Step("Загрузить аватар из файла: {avatarPath}")
  public ProfilePage uploadAvatar(Path avatarPath) {
    avatarInput.setInputFiles(avatarPath);
    return this;
  }

  @Step("Сохранить профиль")
  public ProfilePage clickSave() {
    page.waitForResponse(response -> response.url().contains("/graphql")
        && response.request().postData() != null
        && response.request().postData().contains("UpdateUser"), saveButton::click);
    return this;
  }

  @Step("Сбросить изменения профиля")
  public ProfilePage clickReset() {
    resetButton.click();
    return this;
  }

  @Step("Перезагрузить страницу профиля")
  public ProfilePage reload() {
    page.reload();
    return this;
  }

  @Step("Проверить имя: {firstName}")
  public ProfilePage checkThatFirstName(String firstName) {
    PlaywrightAssertions.assertThat(firstNameInput).hasValue(firstName);
    return this;
  }

  @Step("Проверить фамилию: {surname}")
  public ProfilePage checkThatSurname(String surname) {
    PlaywrightAssertions.assertThat(surnameInput).hasValue(surname);
    return this;
  }

  @Step("Проверить страну профиля: {countryName}")
  public ProfilePage checkThatLocation(String countryName) {
    PlaywrightAssertions.assertThat(locationSelect).hasText(countryName);
    return this;
  }

  @Step("Проверить, что аватар отображается")
  public ProfilePage checkThatAvatarIsDisplayed() {
    PlaywrightAssertions.assertThat(avatarImage).isVisible();
    return this;
  }

  @Step("Проверить аватар после загрузки из файла: {avatarPath}")
  public ProfilePage checkThatAvatarMatches(Path avatarPath) throws IOException {
    PlaywrightAssertions.assertThat(avatarImage)
        .hasAttribute("src", Pattern.compile("^data:image/[^;]+;base64,"
            + Pattern.quote(Base64.getEncoder().encodeToString(Files.readAllBytes(avatarPath))) + "$"));
    return this;
  }
}
