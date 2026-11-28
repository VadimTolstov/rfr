package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.AllureEnvironmentExtension;
import io.student.rangiffler.jupiter.extension.DatabasePoolsExtension;
import io.student.rangiffler.jupiter.extension.StandReadinessExtension;
import io.student.rangiffler.jupiter.extension.StandSchemaExtension;
import io.student.rangiffler.jupiter.extension.SuiteExtension;
import io.student.rangiffler.jupiter.probe.StandReadinessExtensionTest.StandFixture;
import java.io.IOException;
import java.io.File;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.jspecify.annotations.NullMarked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@NullMarked
final class AllureEnvironmentExtensionTest {
    private static final String ENVIRONMENT_DB = "rfr.environmentDb";

    @Test
    @DisplayName("По умолчанию Allure записывает окружение без подключений к MySQL")
    void shouldDisableDatabaseByDefault(@TempDir Path directory) throws Exception {
        Path output = directory.resolve("default-environment-db.txt");
        Path arguments = directory.resolve("java-arguments");
        Files.writeString(arguments, "-cp\n" + argument(classpath()) + "\n"
                + DefaultEnvironmentProbe.class.getName() + "\n" + argument(directory.toString()));
        ProcessBuilder builder = new ProcessBuilder(
                ProcessHandle.current().info().command().orElseThrow(),
                "@" + arguments);
        Set<String> inherited = Set.of("RFR_ENVIRONMENT_DB", "JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS");
        builder.environment().keySet().removeIf(key -> inherited.stream().anyMatch(key::equalsIgnoreCase));
        Process process = builder.redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Default configuration probe timed out");
            assertEquals(0, process.exitValue(), () -> "Default configuration probe exited with " + process.exitValue());
            assertEquals("unavailable", environment(directory).getProperty("migration.rangiffler-auth"));
            assertEquals("unavailable", environment(directory).getProperty("migration.rangiffler-api"));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly().waitFor(5, TimeUnit.SECONDS);
            }
        }
    }

    private String classpath() {
        // Gradle's worker launcher may keep the test runtime URLs outside java.class.path.
        return Stream.concat(
                Stream.of(System.getProperty("java.class.path").split(java.util.regex.Pattern.quote(File.pathSeparator))),
                Stream.iterate(Thread.currentThread().getContextClassLoader(), Objects::nonNull, ClassLoader::getParent)
                        .filter(URLClassLoader.class::isInstance)
                        .map(URLClassLoader.class::cast)
                        .flatMap(loader -> Stream.of(loader.getURLs()))
                        .filter(url -> url.getProtocol().equals("file"))
                        .map(url -> Path.of(URI.create(url.toExternalForm())).toString()))
                .distinct().collect(Collectors.joining(File.pathSeparator));
    }

    private String argument(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    @Test
    @DisplayName("Отключённое чтение версий не открывает подключений и не меняет свойства JVM")
    void doesNotConnectOrChangeParentPropertiesWhenDisabled(@TempDir Path directory) throws IOException {
        List<String> keys = List.of(ENVIRONMENT_DB, "rfr.dbUrl", "allure.results.directory", "rfr.commit");
        List<String> before = keys.stream().map(System::getProperty).toList();
        try (ConnectionCounter database = new ConnectionCounter()) {
            database.start();
            EngineExecutionResults results = run(SuccessTargetTest.class, directory,
                    Map.of("rfr.dbUrl", database.address(), ENVIRONMENT_DB, "false"));
            Properties environment = environment(directory);
            assertEquals(Map.of("tests", 1L, "connections", 0, "auth", "unavailable", "api", "unavailable"),
                    Map.of("tests", results.testEvents().succeeded().count(), "connections", database.connections(),
                            "auth", environment.getProperty("migration.rangiffler-auth", "missing"),
                            "api", environment.getProperty("migration.rangiffler-api", "missing")));
        }
        assertTrue(before.equals(keys.stream().map(System::getProperty).toList()),
                "Nested launcher changed JVM properties");
    }

    @Test
    @DisplayName("После прогона environment.properties содержит все ключи")
    void writesEnvironmentAfterRun(@TempDir Path directory) throws IOException {
        assertEquals(1, run(SuccessTargetTest.class, directory, Map.of(ENVIRONMENT_DB, "false"))
                .testEvents().succeeded().count());
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
        EngineExecutionResults results = run(FailureTargetTest.class, directory, Map.of(ENVIRONMENT_DB, "false"));
        assertEquals(0, results.testEvents().started().count());
        assertEquals(1, results.containerEvents().failed().count());
        assertFalse(Files.exists(directory.resolve("later-callback")));
        assertEquals("abc1234", environment(directory).getProperty("commit"));
    }

    @Test
    @DisplayName("Недоступная MySQL не мешает записать окружение с версиями unavailable")
    void writesUnavailableVersions(@TempDir Path directory) throws IOException {
        // Keep the port reserved while withholding the MySQL handshake.
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            assertEquals(1, run(SuccessTargetTest.class, directory,
                    Map.of("rfr.dbUrl", "jdbc:mysql://127.0.0.1:" + socket.getLocalPort(), ENVIRONMENT_DB, "true"))
                    .testEvents().succeeded().count());
        }
        Properties environment = environment(directory);
        assertEquals("unavailable", environment.getProperty("migration.rangiffler-auth"));
        assertEquals("unavailable", environment.getProperty("migration.rangiffler-api"));
        assertTrue(environment.containsKey("frontUrl"));
    }

    @Test
    @DisplayName("Включённое чтение версий обращается к MySQL и сохраняет unavailable при ошибке")
    void shouldReadDatabaseWhenEnabled(@TempDir Path directory) throws IOException {
        String parent = System.getProperty(ENVIRONMENT_DB, "");
        try (ConnectionCounter database = new ConnectionCounter()) {
            database.start();
            EngineExecutionResults results = run(SuccessTargetTest.class, directory,
                    Map.of("rfr.dbUrl", database.address(), ENVIRONMENT_DB, "true"));
            Properties environment = environment(directory);
            assertEquals(Map.of("tests", 1L, "connected", true, "auth", "unavailable", "api", "unavailable"),
                    Map.of("tests", results.testEvents().succeeded().count(), "connected", database.connections() > 0,
                            "auth", environment.getProperty("migration.rangiffler-auth", "missing"),
                            "api", environment.getProperty("migration.rangiffler-api", "missing")));
        }
        assertTrue(parent.equals(System.getProperty(ENVIRONMENT_DB, "")), "Nested launcher changed rfr.environmentDb");
    }
    @Test
    @DisplayName("Allure записывает окружение, когда ожидание стенда блокирует UI-класс")
    void writesEnvironmentAfterReadinessTimeout(@TempDir Path directory) throws IOException {
        try (StandFixture stand = new StandFixture()) {
            stand.start(Duration.ofDays(1), false);
            Map<String, String> parameters = new HashMap<>(stand.parameters(true, 2));
            parameters.put(ENVIRONMENT_DB, "false");
            parameters.put("rfr.migrationCheck", "true");
            parameters.put("rfr.authMigrations", directory.resolve("missing").toString());
            parameters.put("rfr.apiMigrations", directory.resolve("missing").toString());
            EngineExecutionResults results = run(ReadinessTimeoutTargetTest.class, directory, parameters,
                    String.join(",", AllureEnvironmentExtension.class.getName(), DatabasePoolsExtension.class.getName(),
                            StandReadinessExtension.class.getName(),
                            StandSchemaExtension.class.getName()));
            assertEquals(0, results.testEvents().started().count());
            assertTrue(results.containerEvents().failed().stream()
                    .flatMap(event -> event.getRequiredPayload(TestExecutionResult.class).getThrowable().stream())
                    .anyMatch(error -> error.getMessage().contains("Stand did not become ready")
                            && error.getMessage().contains("rfr-gql-client")));
            assertEquals(parameters.get("rfr.frontUrl"), environment(directory).getProperty("frontUrl"));
        }
    }

    private EngineExecutionResults run(Class<?> target, Path directory, Map<String, String> parameters) {
        return run(target, directory, parameters, AllureEnvironmentExtension.class.getName());
    }

    private EngineExecutionResults run(Class<?> target, Path directory, Map<String, String> parameters,
            String extensions) {
        return EngineTestKit.engine("junit-jupiter").selectors(selectClass(target))
                .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.include", extensions)
                .configurationParameter("allure.results.directory", directory.toString())
                .configurationParameter("rfr.frontUrl", "http://127.0.0.1:3001")
                .configurationParameter("rfr.authUrl", "http://127.0.0.1:9001")
                .configurationParameter("rfr.apiUrl", "http://127.0.0.1:8081")
                .configurationParameter("rfr.dbUser", "probe")
                .configurationParameter("rfr.dbPassword", "probe")
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
            this(new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")), new AtomicInteger(),
                    Executors.newVirtualThreadPerTaskExecutor());
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

    /** Exercises the global callbacks when the frontend never gets ready. */
    @ProbeTarget
    @Tag("ui")
    static final class ReadinessTimeoutTargetTest {
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
    /** Runs the actual Allure default branch without inheriting launcher settings. */
    static final class DefaultEnvironmentProbe {
        public static void main(String[] args) throws IOException {
            Path directory = Path.of(args[0]);
            try (ConnectionCounter database = new ConnectionCounter()) {
                database.start();
                EngineExecutionResults results = new AllureEnvironmentExtensionTest().run(
                        SuccessTargetTest.class, directory, Map.of("rfr.dbUrl", database.address()));
                assertEquals(1, results.testEvents().succeeded().count());
                assertEquals(0, database.connections());
            }
        }
    }
}
