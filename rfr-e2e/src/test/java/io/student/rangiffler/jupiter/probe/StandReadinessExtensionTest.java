package io.student.rangiffler.jupiter.probe;

import com.sun.net.httpserver.HttpServer;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.StandReadinessExtension;
import io.student.rangiffler.jupiter.extension.AllureEnvironmentExtension;
import io.student.rangiffler.jupiter.extension.StandSchemaExtension;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.Extension;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/** Exercises stand readiness without depending on the running application. */
final class StandReadinessExtensionTest {
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
    @DisplayName("ServiceLoader регистрирует ожидание до сверки миграций")
    void readinessPrecedesSchema() {
        List<String> extensions = ServiceLoader.load(Extension.class).stream()
                .map(provider -> provider.type().getName()).toList();
        assertTrue(extensions.contains(StandReadinessExtension.class.getName()));
        assertTrue(extensions.indexOf(StandSchemaExtension.class.getName())
                > extensions.indexOf(StandReadinessExtension.class.getName()));
    }

    private EngineExecutionResults run(Map<String, String> parameters) {
        return EngineTestKit.engine("junit-jupiter")
                .selectors(selectClass(ReadinessTargetTest.class))
                .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.exclude",
                        AllureEnvironmentExtension.class.getName())
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
    static final class ReadinessTargetTest {
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
            this(HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0),
                    new ServerSocket(0, 50, InetAddress.getLoopbackAddress()),
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
            String address = "http://127.0.0.1:" + server.getAddress().getPort();
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
