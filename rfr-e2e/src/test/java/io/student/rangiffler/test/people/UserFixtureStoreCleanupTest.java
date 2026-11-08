package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.config.PlaywrightOptionsFactory;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.UserFixture;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.request.UserRequest;
import io.student.rangiffler.ui.page.LoginPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@UsePlaywright(PlaywrightOptionsFactory.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
public final class UserFixtureStoreCleanupTest {
  private final UserFixture target = new UserRequest(0, 0).fixture();

  @BeforeAll
  void createTarget() {
    target.create();
  }

  @Test
  @Order(1)
  @DisplayName("Связь, созданная в браузере, удаляется при закрытии Store")
  void shouldRemoveBrowserCreatedInvitationWhenStoreCloses(@User TestUser user, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .clickPeople()
        .clickAllPeopleTab()
        .searchPeople(target.user().username())
        .clickAddUser(target.user().username())
        .checkThatInvitationWasSent(target.user().username());
  }

  @Test
  @Order(2)
  @DisplayName("После закрытия Store у целевого пользователя нет входящей заявки")
  void shouldNotShowInvitationAfterStoreCloses(Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(target.user().username(), target.user().password())
        .clickSignIn()
        .clickPeople()
        .clickIncomeInvitationsTab()
        .checkThatIncomeInvitationsCount(0);
  }

  @AfterAll
  void closeTarget() {
    target.close();
  }
}
