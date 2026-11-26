package io.student.rangiffler.test;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
public final class MainPageTest {
  @Test
  @DisplayName("Успешное отображение главной страницы после входа")
  void shouldOpenMainPageAfterLogin(@User TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .checkThatTravelsMapVisible();
  }

  @Test
  @DisplayName("Воспроизвести неоднозначный поиск кнопки like на странице")
  void shouldDemonstratePageWideLikeStrictMode(Page page) {
    page.setContent("""
        <article><p>First trip</p><button aria-label="like"></button><p>2 likes</p></article>
        <article><p>Second trip</p><button aria-label="like"></button><p>12 likes</p></article>
        """);
    PlaywrightAssertions.assertThat(page.getByRole(AriaRole.ARTICLE)).hasCount(2);
    PlaywrightAssertions.assertThat(page.getByRole(
        AriaRole.BUTTON, new Page.GetByRoleOptions().setName("like").setExact(true)
    )).isVisible();
  }
}
