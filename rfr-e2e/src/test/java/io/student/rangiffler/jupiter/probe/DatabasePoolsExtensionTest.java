package io.student.rangiffler.jupiter.probe;

import com.zaxxer.hikari.HikariDataSource;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.DatabasePoolsExtension;
import io.student.rangiffler.jupiter.extension.UserExtension;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/** Exercises real Hikari resources without a connection to MySQL. */
final class DatabasePoolsExtensionTest {
    @Test
    @DisplayName("JUnit закрывает общие пулы после освобождения последнего владельца")
    void managesPoolLifetimeInIsolatedRuns() throws Exception {
        Thread thread = Thread.currentThread();
        ClassLoader parent = thread.getContextClassLoader();
        try (var loader = new PoolClassLoader(parent)) {
            thread.setContextClassLoader(loader);
            // The Gradle root already owns its pools, so the scenarios need a separate copy of the project classes.
            EngineExecutionResults results = EngineTestKit.engine("junit-jupiter")
                    .selectors(selectClass(loader, Scenarios.class.getName()))
                    .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                    .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "false")
                    .configurationParameter("junit.jupiter.execution.parallel.enabled", "false")
                    .execute();
            assertEquals(7, results.testEvents().succeeded().count(),
                    () -> results.allEvents().failed().list().toString());
        } finally {
            thread.setContextClassLoader(parent);
        }
    }

    /** Isolates the project enums and extension while sharing JUnit and Hikari with Gradle. */
    private static final class PoolClassLoader extends URLClassLoader {
        PoolClassLoader(ClassLoader parent) {
            super(new URL[]{DatabasePoolsExtensionTest.class.getProtectionDomain().getCodeSource().getLocation()},
                    parent);
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.startsWith("io.student.rangiffler.")) {
                return super.loadClass(name, resolve);
            }
            Class<?> type = findLoadedClass(name);
            if (type == null) {
                type = findClass(name);
            }
            if (resolve) {
                resolveClass(type);
            }
            return type;
        }
    }

    /** Coordinates isolated executions and observes pools after their roots have closed. */
    @ProbeTarget
    static final class Scenarios {
        @Test
        void closesAndRecreatesBothPoolsForIndependentNonUiRuns() {
            assertEquals(1, run(PoolTargetTest.class).testEvents().succeeded().count());
            Pools first = OBSERVED.get();
            first.checkClosed();
            assertEquals(1, run(PoolTargetTest.class).testEvents().succeeded().count());
            Pools second = OBSERVED.get();
            second.checkClosed();
            assertNotSame(first.auth(), second.auth());
            assertNotSame(first.api(), second.api());
        }

        @Test
        void doesNotCreatePoolsForTestsWithoutDatabaseUse() {
            assertEquals(1, run(NoDatabaseTargetTest.class).testEvents().succeeded().count());
            checkNoPools();
        }

        @Test
        void invalidUserParametersFailBeforeAllocatingAnyDatabasePool() {
            EngineExecutionResults results = run(UserParametersTarget.class);
            assertEquals(0, results.containerEvents().failed().count());
            assertEquals(1, results.testEvents().succeeded().count());
            assertEquals(2, results.testEvents().failed().stream()
                    .map(event -> event.getRequiredPayload(TestExecutionResult.class).getThrowable().orElseThrow())
                    .map(failure -> assertInstanceOf(ParameterResolutionException.class, failure))
                    .filter(failure -> failure.getMessage().endsWith("must have type TestUser"))
                    .count());
        }

        @Test
        void keepsPoolsOpenAfterNestedRunUntilOuterRunEnds() {
            EngineExecutionResults results = run(NestedTargetTest.class);
            assertEquals(1, results.testEvents().succeeded().count(), () -> results.testEvents().failed().list().toString());
            OBSERVED.get().checkClosed();
        }

        @Test
        void closesPoolsDespiteTestFailure() {
            assertFailure(run(FailingMethodTargetTest.class).testEvents().failed(), "Probe test failure");
            OBSERVED.get().checkClosed();
        }

        @Test
        void closesPoolsDespiteBeforeAllFailure() {
            EngineExecutionResults results = run(FailingBeforeAllTargetTest.class);
            assertFailure(results.containerEvents().failed(), "Probe beforeAll failure");
            assertEquals(0, results.testEvents().started().count());
            OBSERVED.get().checkClosed();
        }

        @Test
        void keepsPoolsOpenUntilBothConcurrentRootsHaveFinished() throws Exception {
            Pair pair = new Pair(new CountDownLatch(2), new CountDownLatch(1), new CountDownLatch(1));
            ACTIVE.set(pair);
            var executor = Executors.newVirtualThreadPerTaskExecutor();
            try {
                var first = executor.submit(() -> run(FirstConcurrentTargetTest.class));
                var second = executor.submit(() -> run(SecondConcurrentTargetTest.class));
                assertTrue(pair.entered().await(10, TimeUnit.SECONDS));
                Pools pools = OBSERVED.get();
                pair.releaseFirst().countDown();
                assertEquals(1, first.get(10, TimeUnit.SECONDS).testEvents().succeeded().count());
                pools.checkOpen();
                pair.second().countDown();
                assertEquals(1, second.get(10, TimeUnit.SECONDS).testEvents().succeeded().count());
                pools.checkClosed();
            } finally {
                pair.releaseFirst().countDown();
                pair.second().countDown();
                executor.shutdownNow();
            }
        }
    }

    private static final AtomicReference<Pools> OBSERVED = new AtomicReference<>();
    private static final AtomicReference<Pair> ACTIVE = new AtomicReference<>();

    private static EngineExecutionResults run(Class<?> target) {
        return EngineTestKit.engine("junit-jupiter").selectors(selectClass(target))
                .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.include", DatabasePoolsExtension.class.getName())
                .configurationParameter("junit.jupiter.extensions.store.close.autocloseable.enabled", "true")
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "false")
                .configurationParameter("rfr.standCheck", "true")
                .configurationParameter("rfr.migrationCheck", "true")
                .execute();
    }

    private static Pools pools() {
        return new Pools(assertInstanceOf(HikariDataSource.class, Databases.AUTH.dataSource()),
                assertInstanceOf(HikariDataSource.class, Databases.API.dataSource()));
    }

    /** Observes pool allocation before the nested root can release its lease. */
    @ProbeTarget
    @ExtendWith(UserExtension.class)
    static final class UserParametersTarget {
        @Test
        void withoutUser() {
            checkNoPools();
        }

        @Test
        void wrongType(@User String invalid) {
            throw new AssertionError("Invalid User parameter reached the test");
        }

        @Test
        void wrongTypeAfterValid(@User TestUser valid, @User String invalid) {
            throw new AssertionError("Invalid User signature reached the test");
        }

        @AfterAll
        static void checkPoolsBeforeRootCloses() {
            checkNoPools();
        }
    }

    private static void assertFailure(Events failures, String message) {
        assertEquals(List.of(message), failures.stream()
                .map(event -> event.getRequiredPayload(TestExecutionResult.class).getThrowable().orElseThrow().getMessage())
                .toList());
    }

    private static void checkNoPools() {
        for (Databases database : Databases.values()) {
            assertNull(database.getDataSource());
            assertNull(database.getTransactionManager());
        }
    }

    /** Holds references to resources whose close state must survive clearing the enum fields. */
    private record Pools(HikariDataSource auth, HikariDataSource api) {
        void checkOpen() {
            for (HikariDataSource pool : List.of(auth, api)) {
                assertFalse(pool.isClosed());
                assertNull(pool.getHikariPoolMXBean(), "Obtaining the lazy datasource must not open MySQL connections");
            }
            assertSame(auth, Databases.AUTH.getDataSource());
            assertSame(api, Databases.API.getDataSource());
        }

        void checkClosed() {
            assertTrue(auth.isClosed());
            assertTrue(api.isClosed());
            checkNoPools();
        }
    }

    /** Makes two root contexts overlap until the observing test releases them in order. */
    private record Pair(CountDownLatch entered, CountDownLatch releaseFirst, CountDownLatch second) {
        void hold(CountDownLatch release) throws InterruptedException {
            OBSERVED.set(pools());
            entered.countDown();
            assertTrue(release.await(10, TimeUnit.SECONDS));
        }
    }

    /** Allocates both lazy data sources within an owned test run. */
    @ProbeTarget
    static final class PoolTargetTest {
        @Test
        void opensPools() {
            Pools pools = pools();
            pools.checkOpen();
            OBSERVED.set(pools);
        }
    }

    /** Observes an otherwise idle run before its lease is released. */
    @ProbeTarget
    static final class NoDatabaseTargetTest {
        @Test
        void leavesPoolsUnallocated() {
            checkNoPools();
        }
    }

    /** Keeps the outer owner active while a nested engine run reuses the same data sources. */
    @ProbeTarget
    static final class NestedTargetTest {
        @Test
        void keepsOuterPools() {
            Pools pools = pools();
            assertEquals(1, run(PoolTargetTest.class).testEvents().succeeded().count());
            assertEquals(pools, OBSERVED.get());
            pools.checkOpen();
        }
    }

    /** Fails from a test method after allocating pools. */
    @ProbeTarget
    static final class FailingMethodTargetTest {
        @Test
        void fails() {
            OBSERVED.set(pools());
            throw new IllegalStateException("Probe test failure");
        }
    }

    /** Reaches only the failure callback after the global pool callback registered its owner. */
    @ProbeTarget
    @ExtendWith(FailingCallback.class)
    static final class FailingBeforeAllTargetTest {
        @Test
        void executes() {
        }
    }

    /** Allocates pools and then deliberately fails class preparation. */
    static final class FailingCallback implements BeforeAllCallback {
        @Override
        public void beforeAll(ExtensionContext context) {
            OBSERVED.set(pools());
            throw new IllegalStateException("Probe beforeAll failure");
        }
    }

    /** Holds the first of two concurrent owners. */
    @ProbeTarget
    static final class FirstConcurrentTargetTest {
        @Test
        void holdsPools() throws InterruptedException {
            Pair pair = ACTIVE.get();
            pair.hold(pair.releaseFirst());
        }
    }

    /** Holds the second of two concurrent owners. */
    @ProbeTarget
    static final class SecondConcurrentTargetTest {
        @Test
        void holdsPools() throws InterruptedException {
            Pair pair = ACTIVE.get();
            pair.hold(pair.second());
        }
    }
}
