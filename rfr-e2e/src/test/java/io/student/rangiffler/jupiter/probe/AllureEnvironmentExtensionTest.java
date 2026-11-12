package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.AllureEnvironmentExtension;
import io.student.rangiffler.jupiter.extension.SuiteExtension;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.jspecify.annotations.NullMarked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/** Verifies environment publication and callback ordering through the Jupiter engine. */
@NullMarked
final class AllureEnvironmentExtensionTest {
    @Test
    @DisplayName("По умолчанию окружение записывается без подключений к MySQL")
    void shouldWriteEnvironmentWithoutDatabaseByDefault(@TempDir Path directory) throws IOException {
        try (ConnectionCounter database = new ConnectionCounter()) {
            database.start();
            EngineExecutionResults results = run(SuccessTargetTest.class, directory,
                    Map.of("rfr.dbUrl", database.address()));
            Properties environment = environment(directory);
            assertEquals(Map.of("tests", 1L, "connections", 0, "auth", "unavailable", "api", "unavailable"),
                    Map.of("tests", results.testEvents().succeeded().count(), "connections", database.connections(),
                            "auth", environment.getProperty("migration.rangiffler-auth", "missing"),
                            "api", environment.getProperty("migration.rangiffler-api", "missing")));
        }
    }

    @Test
    @DisplayName("После прогона environment.properties содержит все ключи")
    void writesEnvironmentAfterRun(@TempDir Path directory) throws IOException {
        assertEquals(1, run(SuccessTargetTest.class, directory, Map.of()).testEvents().succeeded().count());
        Properties environment = environment(directory);
        assertEquals(Set.of("frontUrl", "authUrl", "apiUrl", "browser", "headless", "commit",
                "migration.rangiffler-auth", "migration.rangiffler-api"), environment.stringPropertyNames());
        assertEquals("abc1234", environment.getProperty("commit"));
        assertEquals("chromium", environment.getProperty("browser"));
        assertEquals("true", environment.getProperty("headless"));
    }

    @Test
    @DisplayName("После упавшего beforeSuite следующие beforeAll не вызываются, но окружение записывается")
    void writesEnvironmentAfterSetupFailure(@TempDir Path directory) throws IOException {
        EngineExecutionResults results = run(FailureTargetTest.class, directory, Map.of());
        assertEquals(0, results.testEvents().started().count());
        assertEquals(1, results.containerEvents().failed().count());
        assertFalse(Files.exists(directory.resolve("later-callback")));
        assertEquals("abc1234", environment(directory).getProperty("commit"));
        assertEquals(AllureEnvironmentExtension.class,
                ServiceLoader.load(Extension.class).stream().findFirst().orElseThrow().type());
    }

    @Test
    @DisplayName("Недоступная MySQL не мешает записать окружение с версиями unavailable")
    void writesUnavailableVersions(@TempDir Path directory) throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        assertEquals(1, run(SuccessTargetTest.class, directory,
                Map.of("rfr.dbUrl", "jdbc:mysql://127.0.0.1:" + port, "rfr.environmentDb", "true")).testEvents().succeeded().count());
        Properties environment = environment(directory);
        assertEquals("unavailable", environment.getProperty("migration.rangiffler-auth"));
        assertEquals("unavailable", environment.getProperty("migration.rangiffler-api"));
        assertTrue(environment.containsKey("frontUrl"));
    }

    @Test
    @DisplayName("Включённое чтение версий обращается к MySQL и сохраняет unavailable при ошибке")
    void shouldReadDatabaseWhenEnabled(@TempDir Path directory) throws IOException {
        try (ConnectionCounter database = new ConnectionCounter()) {
            database.start();
            EngineExecutionResults results = run(SuccessTargetTest.class, directory,
                    Map.of("rfr.dbUrl", database.address(), "rfr.environmentDb", "true"));
            Properties environment = environment(directory);
            assertEquals(Map.of("tests", 1L, "connected", true, "auth", "unavailable", "api", "unavailable"),
                    Map.of("tests", results.testEvents().succeeded().count(), "connected", database.connections() > 0,
                            "auth", environment.getProperty("migration.rangiffler-auth", "missing"),
                            "api", environment.getProperty("migration.rangiffler-api", "missing")));
        }
    }
    private EngineExecutionResults run(Class<?> target, Path directory, Map<String, String> parameters) {
        return EngineTestKit.engine("junit-jupiter").selectors(selectClass(target))
                .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.include",
                        AllureEnvironmentExtension.class.getName())
                .configurationParameter("allure.results.directory", directory.toString())
                .configurationParameter("rfr.commit", "abc1234")
                .configurationParameter("rfr.browser", "chromium")
                .configurationParameter("rfr.headless", "true")
                .configurationParameters(parameters).execute();
    }

    private Properties environment(Path directory) throws IOException {
        Properties environment = new Properties();
        try (var input = Files.newBufferedReader(directory.resolve("environment.properties"), StandardCharsets.UTF_8)) {
            environment.load(input);
        }
        return environment;
    }

    /** Counts real TCP connections without implementing the MySQL protocol. */
    static final class ConnectionCounter implements AutoCloseable {
        private final ServerSocket socket;
        private final AtomicInteger connections;
        private final ExecutorService executor;

        ConnectionCounter() throws IOException {
            this(new ServerSocket(0), new AtomicInteger(), Executors.newVirtualThreadPerTaskExecutor());
        }

        private ConnectionCounter(ServerSocket socket, AtomicInteger connections, ExecutorService executor) {
            this.socket = socket;
            this.connections = connections;
            this.executor = executor;
        }

        void start() {
            executor.execute(() -> {
                try {
                    while (!socket.isClosed()) {
                        try (Socket _ = socket.accept()) {
                            connections.incrementAndGet();
                        }
                    }
                } catch (IOException failure) {
                    if (!socket.isClosed()) {
                        throw new UncheckedIOException("Cannot count database connections", failure);
                    }
                }
            });
        }

        String address() {
            return "jdbc:mysql://127.0.0.1:" + socket.getLocalPort();
        }

        int connections() {
            return connections.get();
        }

        @Override
        public void close() throws IOException {
            try {
                socket.close();
            } finally {
                executor.shutdownNow();
            }
        }
    }

    /** Allows a successful engine run without UI or database fixtures. */
    @ProbeTarget
    static final class SuccessTargetTest {
        @Test
        void executes() {
        }
    }

    /** Fails preparation before a later extension and the test body. */
    @ProbeTarget
    @ExtendWith({Failure.class, Later.class})
    static final class FailureTargetTest {
        @Test
        void executes() {
        }
    }

    /** Models a failed readiness or schema callback. */
    static final class Failure implements SuiteExtension {
        @Override
        public void beforeSuite(ExtensionContext context) {
            throw new IllegalStateException("Setup failed on purpose");
        }
    }

    /** Records whether Jupiter continued to the next before callback. */
    static final class Later implements BeforeAllCallback {
        @Override
        public void beforeAll(ExtensionContext context) {
            try {
                Files.createFile(Path.of(context.getConfigurationParameter("allure.results.directory")
                        .orElseThrow()).resolve("later-callback"));
            } catch (IOException failure) {
                throw new UncheckedIOException("Cannot record the later callback", failure);
            }
        }
    }
}
