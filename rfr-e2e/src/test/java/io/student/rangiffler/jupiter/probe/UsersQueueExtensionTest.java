package io.student.rangiffler.jupiter.probe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.StaticUserFixture;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.StaticUser;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.UsersQueueExtension;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

@Isolated
public final class UsersQueueExtensionTest {
  @Test
  @DisplayName("Возвращает пользователей в пул после четырёх последовательных тестов")
  void shouldReuseThreeUsersAcrossFourTests() {
    EngineExecutionResults results = execute(FourUsersTarget.class);
    assertEquals(4, results.testEvents().succeeded().count());
    assertEquals(0, results.testEvents().failed().count());
  }

  @Test
  @Timeout(15)
  @DisplayName("После падения теста все три пары доступны одновременно")
  void shouldReturnUserAfterTestFailure() {
    EngineExecutionResults failed = execute(FailingUserTarget.class);
    assertEquals(1, failed.testEvents().failed().count());
    assertEquals(1, failed.testEvents().failed().stream()
        .map(event -> event.getRequiredPayload(TestExecutionResult.class).getThrowable().orElseThrow())
        .filter(failure -> failure instanceof AssertionError
            && failure.getMessage().startsWith("Intentional failure after static user lease "))
        .count());
    execute(AllUsersTarget.class).testEvents()
        .assertStatistics(statistics -> statistics.started(1).succeeded(1).failed(0));
    EngineExecutionResults recovered = execute(FourUsersTarget.class);
    assertEquals(4, recovered.testEvents().succeeded().count());
    assertEquals(0, recovered.testEvents().failed().count());
    execute(AllUsersTarget.class).testEvents()
        .assertStatistics(statistics -> statistics.started(1).succeeded(1).failed(0));
  }

  @Test
  @DisplayName("Выполняет тест без параметра с аннотацией StaticUser")
  void shouldRunTestWithoutStaticUserAnnotation() {
    assertEquals(1, execute(MissingStaticUserAnnotationTarget.class).testEvents().succeeded().count());
  }

  @Test
  @DisplayName("Некорректная сигнатура отклоняется даже при занятых парах пользователей")
  void rejectsInvalidSignaturesBeforeLeasing() {
    try (var first = new StaticUserFixture();
         var second = new StaticUserFixture();
         var third = new StaticUserFixture()) {
      first.create();
      second.create();
      third.create();
      EngineExecutionResults results = execute(StaticUserParametersTarget.class);
      assertEquals(0, results.containerEvents().failed().count());
      assertEquals(1, results.testEvents().succeeded().count());
      assertEquals(2, results.testEvents().failed().stream()
          .map(event -> event.getRequiredPayload(TestExecutionResult.class).getThrowable().orElseThrow())
          .map(failure -> assertInstanceOf(ParameterResolutionException.class, failure))
          .filter(failure -> failure.getMessage().endsWith("must have type TestUser"))
          .count());
    }
  }

  private EngineExecutionResults execute(Class<?> target) {
    return EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(target))
        .configurationParameter(ProbeTargetCondition.PARAMETER, Boolean.TRUE.toString())
        .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "false")
        .configurationParameter("junit.jupiter.extensions.store.close.autocloseable.enabled", Boolean.TRUE.toString())
        .execute();
  }

  /** Runs invalid signatures while every pooled user belongs to the enclosing test. */
  @ProbeTarget
  @ExtendWith(UsersQueueExtension.class)
  static final class StaticUserParametersTarget {
    @Test
    void withoutStaticUser() {
    }

    @Test
    void wrongType(@StaticUser String invalid) {
      fail("Invalid StaticUser parameter reached the test");
    }

    @Test
    void wrongTypeAfterValid(@StaticUser TestUser valid, @StaticUser String invalid) {
      fail("Invalid StaticUser signature reached the test");
    }
  }

  @ProbeTarget
  @Execution(ExecutionMode.SAME_THREAD)
  static class FourUsersTarget {
    @Test
    void receivesFirstUser(@StaticUser TestUser user) {
      assertFalse(user.username().isBlank());
    }

    @Test
    void second(@StaticUser TestUser user) {
      assertFalse(user.username().isBlank());
    }

    @Test
    void third(@StaticUser TestUser user) {
      assertFalse(user.username().isBlank());
    }

    @Test
    void fourth(@StaticUser TestUser user) {
      assertFalse(user.username().isBlank());
    }
  }

  /** Holds all queue entries at once until this test's Store closes. */
  @ProbeTarget
  static final class AllUsersTarget {
    @Test
    void leasesAllPairs(@StaticUser TestUser first, @StaticUser TestUser second, @StaticUser TestUser third) {
      assertEquals(3, Set.of(first.username(), second.username(), third.username()).size());
    }
  }

  /** Fails after obtaining a user so Store cleanup can be checked. */
  @ProbeTarget
  static class FailingUserTarget {
    @Test
    void failsAfterLeasingUser(@StaticUser TestUser user) {
      fail("Intentional failure after static user lease " + user.username());
    }
  }

  @ProbeTarget
  @ExtendWith(UsersQueueExtension.class)
  static class MissingStaticUserAnnotationTarget {
    @Test
    void runsWithoutStaticUser(TestReporter reporter) {
      reporter.publishEntry("user-lease", "not-required");
    }
  }
}
