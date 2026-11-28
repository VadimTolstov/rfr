package io.student.rangiffler.test.people;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.db.ApiProfile;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.DatabasePoolsExtension;
import io.student.rangiffler.ui.page.LoginPage;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

/** Verifies browser-created invitations are removed with the sender's Store. */
@Tag("ui")
public final class UserFixtureStoreCleanupTest {
  private static final String USERNAME = "rfr.probe.recipient.username";
  private static final String PASSWORD = "rfr.probe.recipient.password";
  static final String SENDER = "sender";

  @Test
  @DisplayName("Связь, созданная в браузере, удаляется при закрытии Store")
  void shouldRemoveBrowserCreatedInvitationWhenStoreCloses(@User TestUser recipient, @User TestUser unrelated) {
    EngineExecutionResults results = verify(SendInvitation.class, Map.of(USERNAME, recipient.username()));
    String sender = results.allEvents().reportingEntryPublished().stream()
        .map(event -> event.getRequiredPayload(ReportEntry.class).getKeyValuePairs())
        .filter(entry -> entry.containsKey(SENDER))
        .map(entry -> entry.get(SENDER))
        .findFirst().orElseThrow();
    assertFalse(new Friendship(sender, recipient.username(), FriendshipStatus.PENDING).exists());
    assertFalse(new ApiProfile(sender).exists());
    assertTrue(new ApiProfile(recipient.username()).exists());
    assertTrue(new AuthAccount(recipient.username(), recipient.password()).exists());
    assertTrue(new ApiProfile(unrelated.username()).exists());
    assertTrue(new AuthAccount(unrelated.username(), unrelated.password()).exists());
    verify(CheckInbox.class, Map.of(USERNAME, recipient.username(), PASSWORD, recipient.password()));
  }

  private EngineExecutionResults verify(Class<?> target, Map<String, String> parameters) {
    EngineExecutionResults results = EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(target))
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
        .configurationParameter("junit.jupiter.extensions.autodetection.include", DatabasePoolsExtension.class.getName())
        .configurationParameter("junit.jupiter.extensions.store.close.autocloseable.enabled", "true")
        // Playwright 1.63 keeps its instance on the worker thread after the enclosing Store closes it.
        .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
        .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "fixed")
        .configurationParameter("junit.jupiter.execution.parallel.config.fixed.parallelism", "1")
        .configurationParameter("junit.jupiter.execution.parallel.config.fixed.max-pool-size", "1")
        .configurationParameters(parameters)
        .execute();
    results.allEvents().failed().stream()
        .map(event -> event.getRequiredPayload(TestExecutionResult.class))
        .flatMap(result -> result.getThrowable().stream())
        .findFirst().ifPresent(failure -> {
          throw new AssertionError("Browser probe failed: " + target.getSimpleName(), failure);
        });
    results.testEvents().assertStatistics(statistics -> statistics.started(1).succeeded(1).failed(0));
    results.containerEvents().assertStatistics(statistics -> statistics.failed(0));
    return results;
  }

  /** Supplies recipient data only through the configuration of the current probe. */
  static final class Recipient implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
      return (parameter.getParameter().getType().equals(String.class)
          || parameter.getParameter().getType().equals(TestUser.class) && !parameter.isAnnotated(User.class))
          && context.getTestMethod().filter(parameter.getDeclaringExecutable()::equals).isPresent();
    }

    @Override
    public Object resolveParameter(ParameterContext parameter, ExtensionContext context) {
      String username = context.getConfigurationParameter(USERNAME).orElseThrow();
      if (parameter.getParameter().getType().equals(String.class)) {
        return username;
      }
      return new TestUser(username, context.getConfigurationParameter(PASSWORD).orElseThrow(),
          List.of(), List.of(), List.of());
    }
  }
}

/** Creates and observes an invitation before the sender's fixture closes. */
@UiTest
@ProbeTarget
@ExtendWith(UserFixtureStoreCleanupTest.Recipient.class)
final class SendInvitation {
  @Test
  @DisplayName("Отправить заявку и подтвердить её отправку")
  void sends(@User TestUser sender, String recipient, Page page, TestReporter reporter) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(sender.username(), sender.password())
        .clickSignIn()
        .clickPeople()
        .clickAllPeopleTab()
        .searchPeople(recipient)
        .clickAddUser(recipient)
        .checkThatInvitationWasSent(recipient);
    reporter.publishEntry(UserFixtureStoreCleanupTest.SENDER, sender.username());
  }
}

/** Reads the recipient's inbox in a fresh browser after the sender's Store closes. */
@UiTest
@ProbeTarget
@ExtendWith(UserFixtureStoreCleanupTest.Recipient.class)
final class CheckInbox {
  @Test
  @DisplayName("После закрытия Store у получателя нет входящей заявки")
  void checks(TestUser recipient, Page page) {
    new LoginPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(recipient.username(), recipient.password())
        .clickSignIn()
        .clickPeople()
        .clickIncomeInvitationsTab()
        .checkThatIncomeInvitationsCount(0);
  }
}
