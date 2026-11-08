package io.student.rangiffler.jupiter.probe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.db.ApiProfile;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.UserExtension;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

/** Exercises UserExtension through the Jupiter engine. */
public final class UserExtensionTest {
  private static final String UNUSED_PASSWORD = "unused-for-existence-check";

  @Test
  @DisplayName("Создаёт пользователя с другом и пригласившим и удаляет данные при закрытии Store")
  void shouldCreateFixtureAndCloseStore(TestReporter reporter) {
    EngineExecutionResults results = execute(ValidUserTarget.class);
    assertEquals(1, results.testEvents().succeeded().count());
    Map<String, String> entries = reportEntries(results);
    String username = entries.get("username");
    String friend = entries.get("friend");
    String inviter = entries.get("inviter");
    assertAbsent(username);
    assertAbsent(friend);
    assertAbsent(inviter);
    assertFalse(new Friendships(username).exists());
    assertFalse(new Friendship(username, friend, FriendshipStatus.ACCEPTED).exists());
    assertFalse(new Friendship(inviter, username, FriendshipStatus.PENDING).exists());
    reporter.publishEntry("fixture-lifecycle", "created-and-removed");
  }

  @Test
  @DisplayName("Закрывает Store после падения теста")
  void shouldCloseStoreAfterTestFailure(TestReporter reporter) {
    EngineExecutionResults results = execute(FailingUserTarget.class);
    assertEquals(1, results.testEvents().failed().count());
    String username = reportEntries(results).get("username");
    assertAbsent(username);
    reporter.publishEntry("failed-test-fixture", "removed");
  }

  @Test
  @DisplayName("Разрешает два параметра с отдельными фикстурами по индексам")
  void shouldResolveDifferentUsersForDifferentParameterIndexes(TestReporter reporter) {
    EngineExecutionResults results = execute(MultipleUsersTarget.class);
    assertEquals(1, results.testEvents().succeeded().count());
    Map<String, String> entries = reportEntries(results);
    String first = entries.get("first");
    String second = entries.get("second");
    assertNotEquals(first, second);
    assertAbsent(first);
    assertAbsent(second);
    reporter.publishEntry("parameter-fixtures", "distinct-and-removed");
  }

  @Test
  @DisplayName("Отклоняет отрицательное количество друзей из UserRequest")
  void shouldRejectNegativeFixtureCounts(TestReporter reporter) {
    EngineExecutionResults results = execute(NegativeCountTarget.class);
    Throwable failure = failure(results);
    assertTrue(hasCause(failure, IllegalArgumentException.class));
    reporter.publishEntry("negative-friends", "rejected");
  }

  @Test
  @DisplayName("Не разрешает TestUser без аннотации User")
  void shouldRejectTestUserWithoutAnnotation(TestReporter reporter) {
    EngineExecutionResults results = execute(UnannotatedUserTarget.class);
    assertTrue(hasCause(failure(results), ParameterResolutionException.class));
    reporter.publishEntry("unannotated-user", "not-resolved");
  }

  @Test
  @DisplayName("Не разрешает User в параметре BeforeEach")
  void shouldRejectUserOnBeforeEachParameter(TestReporter reporter) {
    EngineExecutionResults results = execute(BeforeEachUserTarget.class);
    assertTrue(hasCause(failure(results), ParameterResolutionException.class));
    reporter.publishEntry("before-each-user", "not-resolved");
  }

  @Test
  @DisplayName("Сообщает об отсутствии аннотации User в тестовом методе")
  void shouldFailWhenUserAnnotationIsMissing(TestReporter reporter) {
    EngineExecutionResults results = execute(MissingUserAnnotationTarget.class);
    Throwable failure = failure(results);
    assertTrue(hasCause(failure, ParameterResolutionException.class));
    assertTrue(failure.getMessage().contains("User annotation was not found"));
    reporter.publishEntry("missing-user-annotation", "failed-fast");
  }

  private EngineExecutionResults execute(Class<?> target) {
    return EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(target))
        .configurationParameter(ProbeTargetCondition.PARAMETER, Boolean.TRUE.toString())
        .configurationParameter("junit.jupiter.extensions.store.close.autocloseable.enabled", Boolean.TRUE.toString())
        .execute();
  }

  private Map<String, String> reportEntries(EngineExecutionResults results) {
    List<ReportEntry> entries = results.allEvents().reportingEntryPublished().list().stream()
        .map(event -> event.getRequiredPayload(ReportEntry.class))
        .toList();
    assertEquals(1, entries.size());
    return entries.getFirst().getKeyValuePairs();
  }

  private Throwable failure(EngineExecutionResults results) {
    assertEquals(1, results.testEvents().failed().count());
    return results.testEvents().failed().list().getFirst()
        .getRequiredPayload(TestExecutionResult.class)
        .getThrowable()
        .orElseThrow();
  }

  private boolean hasCause(Throwable failure, Class<? extends Throwable> type) {
    for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
      if (type.isInstance(cause)) {
        return true;
      }
    }
    return false;
  }

  private void assertAbsent(String username) {
    assertFalse(new ApiProfile(username).exists());
    assertFalse(new AuthAccount(username, UNUSED_PASSWORD).exists());
  }

  @ProbeTarget
  static class ValidUserTarget {
    @Test
    void receivesUserWithCreatedPreconditions(@User(friends = 1, incomeInvitations = 1) TestUser user, TestReporter reporter) {
      assertEquals(1, user.friends().size());
      assertEquals(1, user.incomeInvitations().size());
      assertTrue(new ApiProfile(user.username()).exists());
      assertTrue(new AuthAccount(user.username(), user.password()).exists());
      assertTrue(new Friendships(user.username()).exists());
      for (TestUser friend : user.friends()) {
        assertTrue(new ApiProfile(friend.username()).exists());
        assertTrue(new AuthAccount(friend.username(), friend.password()).exists());
        assertTrue(new Friendship(user.username(), friend.username(), FriendshipStatus.ACCEPTED).exists());
      }
      for (TestUser inviter : user.incomeInvitations()) {
        assertTrue(new ApiProfile(inviter.username()).exists());
        assertTrue(new AuthAccount(inviter.username(), inviter.password()).exists());
        assertTrue(new Friendship(inviter.username(), user.username(), FriendshipStatus.PENDING).exists());
      }
      reporter.publishEntry(Map.of(
          "username", user.username(),
          "friend", user.friends().getFirst().username(),
          "inviter", user.incomeInvitations().getFirst().username()
      ));
    }
  }

  @ProbeTarget
  static class FailingUserTarget {
    @Test
    void failsAfterFixtureCreation(@User TestUser user, TestReporter reporter) {
      assertTrue(new ApiProfile(user.username()).exists());
      reporter.publishEntry("username", user.username());
      fail("Intentional failure after fixture creation");
    }
  }

  @ProbeTarget
  static class MultipleUsersTarget {
    @Test
    void receivesDistinctUsers(@User TestUser first, @User TestUser second, TestReporter reporter) {
      assertNotEquals(first.username(), second.username());
      assertTrue(new ApiProfile(first.username()).exists());
      assertTrue(new ApiProfile(second.username()).exists());
      reporter.publishEntry(Map.of("first", first.username(), "second", second.username()));
    }
  }

  @ProbeTarget
  static class NegativeCountTarget {
    @Test
    void rejectsNegativeFriends(@User(friends = -1) TestUser user) {
      fail("Negative friend count should be rejected");
    }
  }

  @ProbeTarget
  static class UnannotatedUserTarget {
    @Test
    void rejectsUnannotatedParameter(@User TestUser annotated, TestUser unannotated) {
      fail("Unannotated user should not be resolved");
    }
  }

  @ProbeTarget
  static class BeforeEachUserTarget {
    @BeforeEach
    void setup(@User TestUser user) {
    }

    @Test
    void hasAnnotatedTestParameter(@User TestUser user) {
      fail("BeforeEach user parameter should not be resolved");
    }
  }

  @ProbeTarget
  @ExtendWith(UserExtension.class)
  static class MissingUserAnnotationTarget {
    @Test
    void requiresUserAnnotation(TestReporter reporter) {
      fail("Test without User annotation should not execute");
    }
  }
}
