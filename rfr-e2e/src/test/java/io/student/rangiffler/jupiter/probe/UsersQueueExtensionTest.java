package io.student.rangiffler.jupiter.probe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.StaticUser;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.UsersQueueExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

public final class UsersQueueExtensionTest {
  @Test
  @DisplayName("Возвращает пользователей в пул после четырёх последовательных тестов")
  void shouldReuseThreeUsersAcrossFourTests() {
    EngineExecutionResults results = execute(FourUsersTarget.class);
    assertEquals(4, results.testEvents().succeeded().count());
    assertEquals(0, results.testEvents().failed().count());
  }

  @Test
  @DisplayName("Возвращает пользователя в пул после падения теста")
  void shouldReturnUserAfterTestFailure() {
    EngineExecutionResults failed = execute(FailingUserTarget.class);
    assertEquals(1, failed.testEvents().failed().count());
    EngineExecutionResults recovered = execute(FourUsersTarget.class);
    assertEquals(4, recovered.testEvents().succeeded().count());
    assertEquals(0, recovered.testEvents().failed().count());
  }

  @Test
  @DisplayName("Выполняет тест без параметра с аннотацией StaticUser")
  void shouldRunTestWithoutStaticUserAnnotation() {
    assertEquals(1, execute(MissingStaticUserAnnotationTarget.class).testEvents().succeeded().count());
  }

  private EngineExecutionResults execute(Class<?> target) {
    return EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(target))
        .configurationParameter(ProbeTargetCondition.PARAMETER, Boolean.TRUE.toString())
        .configurationParameter("junit.jupiter.extensions.store.close.autocloseable.enabled", Boolean.TRUE.toString())
        .execute();
  }

  @ProbeTarget
  @TestMethodOrder(OrderAnnotation.class)
  @Execution(ExecutionMode.SAME_THREAD)
  static class FourUsersTarget {
    @Test
    @Order(1)
    void first(@StaticUser TestUser user) {
      assertFalse(user.username().isBlank());
    }

    @Test
    @Order(2)
    void second(@StaticUser TestUser user) {
      assertFalse(user.username().isBlank());
    }

    @Test
    @Order(3)
    void third(@StaticUser TestUser user) {
      assertFalse(user.username().isBlank());
    }

    @Test
    @Order(4)
    void fourth(@StaticUser TestUser user) {
      assertFalse(user.username().isBlank());
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
