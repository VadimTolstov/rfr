package io.student.rangiffler.test.profile;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.config.PlaywrightOptionsFactory;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.LoginPage;
import io.student.rangiffler.utils.RandomDataUtils;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UsePlaywright(PlaywrightOptionsFactory.class)
public final class ProfileTest {
  @Test
  @DisplayName("Сохранение профиля и чтение данных после перезагрузки")
  void shouldSaveProfileAndReadItAfterReload(@User TestUser user, Page page) throws Exception {
    String firstName = RandomDataUtils.randomFirstName();
    String surname = RandomDataUtils.randomSurname();
    Path avatarPath = Path.of(getClass().getResource("/profile/avatar-saved.jpg").toURI());

    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickProfile()
        .fillProfileForm(firstName, surname)
        .selectLocation("France")
        .uploadAvatar(avatarPath)
        .clickSave()
        .reload()
        .checkThatFirstName(firstName)
        .checkThatSurname(surname)
        .checkThatLocation("France")
        .checkThatAvatarIsDisplayed()
        .checkThatAvatarMatches(avatarPath);
  }

  @Test
  @DisplayName("Сброс изменений возвращает сохранённые значения профиля")
  void shouldResetProfileToSavedValues(@User TestUser user, Page page) throws Exception {
    String savedFirstName = RandomDataUtils.randomFirstName();
    String savedSurname = RandomDataUtils.randomSurname();
    String unsavedFirstName = RandomDataUtils.randomFirstName();
    String unsavedSurname = RandomDataUtils.randomSurname();
    Path savedAvatar = Path.of(getClass().getResource("/profile/avatar-saved.jpg").toURI());
    Path changedAvatar = Path.of(getClass().getResource("/profile/avatar-changed.png").toURI());

    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickProfile()
        .fillProfileForm(savedFirstName, savedSurname)
        .selectLocation("France")
        .uploadAvatar(savedAvatar)
        .clickSave()
        .reload()
        .fillProfileForm(unsavedFirstName, unsavedSurname)
        .selectLocation("Germany")
        .uploadAvatar(changedAvatar)
        .clickReset()
        .checkThatFirstName(savedFirstName)
        .checkThatSurname(savedSurname)
        .checkThatLocation("France")
        .checkThatAvatarMatches(savedAvatar);
  }
}
