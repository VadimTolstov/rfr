package io.student.rangiffler.test.demo;

import io.student.rangiffler.jupiter.annotation.Demo;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.DemoExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.EventType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectNestedClass;

/** Verifies the observable lifecycle of each extension registration example. */
@Tag("demo")
@DisplayName("Места установки @ExtendWith")
final class ExtendWithPlacesTest {
    @ParameterizedTest
    @ValueSource(classes = {ExtendWithExamples.OnClass.class, ExtendWithExamples.OnInterface.class,
            ExtendWithExamples.OnMetaAnnotation.class, ExtendWithExamples.OnStaticField.class})
    @DisplayName("Регистрация уровня класса оборачивает оба теста одним циклом beforeAll и afterAll")
    void classRegistrationWrapsTests(Class<?> target) {
        EngineExecutionResults results = run(target);
        assertEquals(List.of("beforeAll", "test", "test", "afterAll"), lifecycle(results));
    }

    @ParameterizedTest
    @ValueSource(classes = {ExtendWithExamples.OnMethod.class, ExtendWithExamples.OnInstanceField.class,
            ExtendWithExamples.OnConstructorParameter.class, ExtendWithExamples.OnTestParameter.class,
            ExtendWithExamples.OnBeforeEachParameter.class, ExtendWithExamples.OnBeforeAllParameter.class})
    @DisplayName("Локальный резолвер параметров выполняет примеры без callbacks уровня класса")
    void localRegistrationResolvesParameters(Class<?> target) {
        EngineExecutionResults results = run(target);
        assertEquals(List.of("test", "test"), lifecycle(results));
    }

    private EngineExecutionResults run(Class<?> target) {
        EngineExecutionResults results = EngineTestKit.engine("junit-jupiter")
                .selectors(selectNestedClass(List.of(ExtendWithExamples.class), target))
                .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "false")
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "false")
                .execute();
        results.testEvents().assertStatistics(statistics -> statistics.started(2).succeeded(2));
        results.containerEvents().assertStatistics(statistics -> statistics.failed(0));
        return results;
    }

    private List<String> lifecycle(EngineExecutionResults results) {
        return results.allEvents().stream().map(this::event).flatMap(Optional::stream).toList();
    }

    private Optional<String> event(Event event) {
        if (event.getType() == EventType.FINISHED && event.getTestDescriptor().isTest()) {
            return Optional.of("test");
        }
        return event.getPayload(ReportEntry.class).map(entry -> entry.getKeyValuePairs().get("demo.lifecycle"));
    }
}

/** Groups the examples so they only run through the explicitly enabled probe. */
@ProbeTarget
final class ExtendWithExamples {
    @ExtendWith(DemoExtension.class)
    interface Extended {
    }

    @Nested
    @ExtendWith(DemoExtension.class)
    @DisplayName("На классе")
    class OnClass {
        @Test
        @DisplayName("Первый тест получает параметр от расширения класса")
        void firstTestResolvesParameter(String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }

        @Test
        @DisplayName("Второй тест получает параметр от того же расширения")
        void secondTestResolvesParameter(String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }
    }

    @Nested
    @DisplayName("На интерфейсе, который реализует класс")
    class OnInterface implements Extended {
        @Test
        @DisplayName("Первый тест получает параметр от расширения интерфейса")
        void firstTestResolvesParameter(String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }

        @Test
        @DisplayName("Второй тест получает параметр от того же расширения")
        void secondTestResolvesParameter(String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }
    }

    @Nested
    @Demo
    @DisplayName("В составной аннотации")
    class OnMetaAnnotation {
        @Test
        @DisplayName("Первый тест получает параметр от расширения из @Demo")
        void firstTestResolvesParameter(String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }

        @Test
        @DisplayName("Второй тест получает параметр от того же расширения")
        void secondTestResolvesParameter(String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }
    }

    @Nested
    @DisplayName("На тестовом методе")
    class OnMethod {
        @Test
        @ExtendWith(DemoExtension.Parameters.class)
        @DisplayName("Первый тест получает параметр от своего расширения")
        void firstTestResolvesParameter(String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }

        @Test
        @ExtendWith(DemoExtension.Parameters.class)
        @DisplayName("Второй тест получает параметр от своего расширения")
        void secondTestResolvesParameter(String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }
    }

    @Nested
    @DisplayName("На статическом поле")
    class OnStaticField {
        @ExtendWith(DemoExtension.class)
        static String field;

        @Test
        @DisplayName("Первый тест получает параметр от расширения статического поля")
        void firstTestResolvesParameter(String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }

        @Test
        @DisplayName("Второй тест получает параметр от того же расширения")
        void secondTestResolvesParameter(String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }
    }

    @Nested
    @DisplayName("На поле экземпляра")
    class OnInstanceField {
        @ExtendWith(DemoExtension.Parameters.class)
        String field;

        @Test
        @DisplayName("Первый тест получает параметр от расширения поля экземпляра")
        void firstTestResolvesParameter(String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }

        @Test
        @DisplayName("Второй тест получает параметр от расширения своего экземпляра")
        void secondTestResolvesParameter(String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }
    }

    @Nested
    @DisplayName("На параметре конструктора")
    class OnConstructorParameter {
        private final String value;

        OnConstructorParameter(@ExtendWith(DemoExtension.Parameters.class) String value) {
            this.value = value;
        }

        @Test
        @DisplayName("Первый тест видит значение из конструктора")
        void firstTestSeesConstructorValue() {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the constructor parameter");
        }

        @Test
        @DisplayName("Второй тест видит значение из конструктора")
        void secondTestSeesConstructorValue() {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the constructor parameter");
        }
    }

    @Nested
    @DisplayName("На параметре тестового метода")
    class OnTestParameter {
        @Test
        @DisplayName("Первый тест получает параметр от своего расширения")
        void firstTestResolvesParameter(@ExtendWith(DemoExtension.Parameters.class) String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }

        @Test
        @DisplayName("Второй тест получает параметр от своего расширения")
        void secondTestResolvesParameter(@ExtendWith(DemoExtension.Parameters.class) String value) {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the parameter");
        }
    }

    @Nested
    @DisplayName("На параметре @BeforeEach")
    class OnBeforeEachParameter {
        private String value;

        @BeforeEach
        void remember(@ExtendWith(DemoExtension.Parameters.class) String value) {
            this.value = value;
        }

        @Test
        @DisplayName("Первый тест видит значение из @BeforeEach")
        void firstTestSeesBeforeEachValue() {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the @BeforeEach parameter");
        }

        @Test
        @DisplayName("Второй тест видит значение из @BeforeEach")
        void secondTestSeesBeforeEachValue() {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the @BeforeEach parameter");
        }
    }

    /** Tests parameter resolution also when a class lifecycle method is invoked. */
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("На параметре @BeforeAll и @AfterAll")
    class OnBeforeAllParameter {
        private String value;

        @BeforeAll
        void remember(@ExtendWith(DemoExtension.Parameters.class) String resolved) {
            value = resolved;
        }

        @AfterAll
        void verify(@ExtendWith(DemoExtension.Parameters.class) String resolved) {
            assertEquals("resolved by DemoExtension", resolved, "DemoExtension did not resolve the @AfterAll parameter");
        }

        @Test
        @DisplayName("Первый тест видит значение из @BeforeAll")
        void firstTestSeesBeforeAllValue() {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the @BeforeAll parameter");
        }

        @Test
        @DisplayName("Второй тест видит значение из @BeforeAll")
        void secondTestSeesBeforeAllValue() {
            assertEquals("resolved by DemoExtension", value, "DemoExtension did not resolve the @BeforeAll parameter");
        }
    }
}
