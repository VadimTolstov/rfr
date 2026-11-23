package io.student.rangiffler.jupiter.probe;

import com.sun.net.httpserver.HttpServer;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.StandReadinessExtension;
import io.student.rangiffler.jupiter.extension.AllureEnvironmentExtension;
import io.student.rangiffler.jupiter.extension.DatabasePoolsExtension;
import io.student.rangiffler.jupiter.extension.StandSchemaExtension;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

final class StandReadinessExtensionTest {
    @ParameterizedTest
    @MethodSource("localhost")
    @DisplayName("Проверка localhost допускает UI-тест, когда HTTP-сервисы слушают только один из адресов")
    void acceptsLocalhostBoundToOneAddress(InetAddress address) throws IOException {
        try (StandFixture stand = new StandFixture(new InetSocketAddress(address, 0))) {
            stand.start(Duration.ZERO, false);
            EngineExecutionResults results = run(stand.parameters(true, 5, "localhost"));
            assertEquals(1, results.testEvents().succeeded().count(), () -> failures(results).toString());
        }
    }

    private static InetAddress[] localhost() throws UnknownHostException {
        return InetAddress.getAllByName("localhost");
    }

    @Test
    @DisplayName("Обычный класс выполняется без запросов при включённом ожидании")
    void enabledCheckDoesNotRequestServicesForNonUiClass() throws IOException {
        try (StandFixture stand = new StandFixture()) {
            stand.start(Duration.ZERO, false);
            EngineExecutionResults results = run(stand.parameters(true, 5), selectClass(NonUiTargetTest.class));
            assertEquals(1, results.testEvents().succeeded().count());
            assertEquals(0, stand.requests());
        }
    }

    @Test
    @DisplayName("Два UI-класса проверяют каждый HTTP-сервис один раз")
    void checksStandOnceForTwoUiClasses() throws IOException {
        try (StandFixture stand = new StandFixture()) {
            stand.start(Duration.ZERO, false);
            EngineExecutionResults results = run(stand.parameters(true, 5),
                    selectClass(ReadinessTargetTest.class), selectClass(SecondUiTargetTest.class));
            assertEquals(2, results.testEvents().succeeded().count());
            assertEquals(3, stand.requests());
        }
    }

    @Test
    @DisplayName("Недоступный фронтенд блокирует UI-класс после успешного обычного класса")
    void startsWaitingOnlyAtUiClass() throws IOException {
        try (StandFixture stand = new StandFixture()) {
            stand.start(Duration.ofDays(1), false);
            EngineExecutionResults results = run(stand.parameters(true, 1),
                    selectClass(NonUiTargetTest.class), selectClass(ReadinessTargetTest.class));
            assertEquals(1, results.testEvents().started().count());
            assertTrue(results.testEvents().succeeded().stream().anyMatch(event ->
                    event.getTestDescriptor().getUniqueId().toString().contains(NonUiTargetTest.class.getName())));
            assertTrue(failures(results).stream().anyMatch(message -> message.contains("rfr-gql-client")));
        }
    }

    @Test
    @DisplayName("Тест выполняется, когда фронтенд начинает отвечать через две секунды")
    void waitsForDelayedService() throws IOException {
        try (StandFixture stand = new StandFixture()) {
            stand.start(Duration.ofSeconds(2), false);
            long started = System.nanoTime();
            EngineExecutionResults results = run(stand.parameters(true, 5));
            assertEquals(1, results.testEvents().succeeded().count());
            assertTrue(System.nanoTime() - started >= Duration.ofSeconds(2).minusMillis(200).toNanos());
            assertTrue(stand.requests() > 3, "Unavailable frontend was not retried");
        }
    }

    @Test
    @DisplayName("Истечение таймаута блокирует тест и называет фронтенд с командой запуска")
    void timeoutPreventsTests() throws IOException {
        try (StandFixture stand = new StandFixture()) {
            stand.start(Duration.ofDays(1), false);
            EngineExecutionResults results = run(stand.parameters(true, 1));
            assertEquals(0, results.testEvents().started().count());
            assertTrue(failures(results).stream().anyMatch(message ->
                    message.contains("rfr-gql-client") && message.contains("npm run dev")));
        }
    }

    @Test
    @DisplayName("Выключенная проверка не отправляет запросы")
    void disabledCheckDoesNotRequestServices() throws IOException {
        try (StandFixture stand = new StandFixture()) {
            stand.start(Duration.ZERO, false);
            assertEquals(1, run(stand.parameters(false, 1)).testEvents().succeeded().count());
            assertEquals(0, stand.requests());
        }
    }

    @Test
    @DisplayName("Зависший HTTP-ответ ограничен общим таймаутом")
    void stalledResponseIsBounded() throws IOException {
        try (StandFixture stand = new StandFixture()) {
            stand.start(Duration.ZERO, true);
            long started = System.nanoTime();
            EngineExecutionResults results = run(stand.parameters(true, 1));
            assertEquals(0, results.testEvents().started().count());
            assertTrue(failures(results).stream().anyMatch(message -> message.contains("rfr-gql-client")));
            assertTrue(System.nanoTime() - started < Duration.ofSeconds(3).toNanos(),
                    "Readiness exceeded the deadline while waiting for an HTTP body");
        }
    }

    @Test
    @DisplayName("ServiceLoader регистрирует окружение и пулы до ожидания и сверки миграций")
    void registersSuiteExtensionsInOrder() {
        List<String> expected = List.of(AllureEnvironmentExtension.class.getName(),
                DatabasePoolsExtension.class.getName(), StandReadinessExtension.class.getName(),
                StandSchemaExtension.class.getName());
        assertEquals(expected, ServiceLoader.load(Extension.class).stream()
                .map(provider -> provider.type().getName()).filter(expected::contains).toList());
    }

    private EngineExecutionResults run(Map<String, String> parameters) {
        return run(parameters, selectClass(ReadinessTargetTest.class));
    }

    private EngineExecutionResults run(Map<String, String> parameters, DiscoverySelector... selectors) {
        return EngineTestKit.engine("junit-jupiter")
                .selectors(selectors)
                .configurationParameter("junit.jupiter.testclass.order.default",
                        "org.junit.jupiter.api.ClassOrderer$OrderAnnotation")
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "false")
                .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.include",
                        StandReadinessExtension.class.getName())
                .configurationParameters(parameters)
                .execute();
    }

    private List<String> failures(EngineExecutionResults results) {
        return results.containerEvents().failed().stream()
                .map(event -> event.getRequiredPayload(TestExecutionResult.class))
                .flatMap(result -> result.getThrowable().stream())
                .map(Throwable::getMessage).toList();
    }

    /** Detects whether Jupiter allowed the test body to start. */
    @ProbeTarget
    @Tag("ui")
    @Order(2)
    static final class ReadinessTargetTest {
        @Test
        void executes() {
        }
    }

    /** Exercises a class that does not require stand readiness. */
    @ProbeTarget
    @Order(1)
    static final class NonUiTargetTest {
        @Test
        void executes() {
        }
    }

    /** Shares a Jupiter run with the first UI class. */
    @ProbeTarget
    @Tag("ui")
    static final class SecondUiTargetTest {
        @Test
        void executes() {
        }
    }

    /** Supplies independent loopback endpoints for each EngineTestKit run. */
    static final class StandFixture implements AutoCloseable {
        private final HttpServer server;
        private final ServerSocket database;
        private final AtomicInteger requests;
        private final ExecutorService executor;

        StandFixture() throws IOException {
            this(new InetSocketAddress("127.0.0.1", 0));
        }

        StandFixture(InetSocketAddress address) throws IOException {
            this(HttpServer.create(address, 0),
                    new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1")),
                    new AtomicInteger(), Executors.newVirtualThreadPerTaskExecutor());
        }

        private StandFixture(HttpServer server, ServerSocket database,
                AtomicInteger requests, ExecutorService executor) {
            this.server = server;
            this.database = database;
            this.requests = requests;
            this.executor = executor;
        }

        void start(Duration delay, boolean stalled) {
            long started = System.nanoTime();
            server.setExecutor(executor);
            server.createContext("/", exchange -> {
                requests.incrementAndGet();
                try (exchange) {
                    if (exchange.getRequestHeaders().containsKey("Upgrade")) {
                        exchange.sendResponseHeaders(400, -1);
                        return;
                    }
                    if (stalled && exchange.getRequestURI().getPath().equals("/")) {
                        exchange.sendResponseHeaders(200, 100);
                        exchange.getResponseBody().flush();
                        Thread.sleep(5000);
                    } else {
                        exchange.sendResponseHeaders(exchange.getRequestURI().getPath().equals("/")
                                && System.nanoTime() - started < delay.toNanos() ? 503 : 200, -1);
                    }
                } catch (InterruptedException failure) {
                    Thread.currentThread().interrupt();
                }
            });
            server.start();
        }

        Map<String, String> parameters(boolean enabled, int timeout) {
            return parameters(enabled, timeout, "127.0.0.1");
        }

        Map<String, String> parameters(boolean enabled, int timeout, String host) {
            String address = "http://" + host + ":" + server.getAddress().getPort();
            return Map.of("rfr.standCheck", Boolean.toString(enabled),
                    "rfr.standTimeout", Integer.toString(timeout),
                    "rfr.dbUrl", "jdbc:mysql://127.0.0.1:" + database.getLocalPort(),
                    "rfr.authUrl", address, "rfr.apiUrl", address, "rfr.frontUrl", address);
        }

        int requests() {
            return requests.get();
        }

        @Override
        public void close() throws IOException {
            server.stop(0);
            executor.shutdownNow();
            database.close();
        }
    }
}
