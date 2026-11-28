package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.DatabasePoolsExtension;
import io.student.rangiffler.jupiter.extension.StandSchemaExtension;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class StandSchemaExtensionTest {
    @Test
    @DisplayName("Обычный класс пропускает сверку при включённом флаге")
    void skipsValidationForNonUiClass(@TempDir Path directory) {
        assertEquals(1, run(NonUiTargetTest.class, directory.resolve("missing")).testEvents().succeeded().count());
    }

    @Test
    @DisplayName("UI-класс блокируется, если каталог миграций отсутствует")
    void validatesMigrationsForUiClass(@TempDir Path directory) {
        Path missing = directory.resolve("missing");
        EngineExecutionResults results = run(UiTargetTest.class, missing);
        assertEquals(0, results.testEvents().started().count());
        assertTrue(results.containerEvents().failed().stream()
                .flatMap(event -> event.getRequiredPayload(TestExecutionResult.class).getThrowable().stream())
                .anyMatch(error -> error.getMessage().contains("Migrations directory " + missing)
                        && error.getMessage().contains("does not exist")));
    }

    private EngineExecutionResults run(Class<?> target, Path missing) {
        return EngineTestKit.engine("junit-jupiter").selectors(selectClass(target))
                .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
                .configurationParameter("junit.jupiter.extensions.autodetection.include",
                        String.join(",", DatabasePoolsExtension.class.getName(), StandSchemaExtension.class.getName()))
                .configurationParameter("rfr.migrationCheck", "true")
                .configurationParameter("rfr.authMigrations", missing.toString())
                .configurationParameter("rfr.apiMigrations", missing.toString())
                .execute();
    }

    /** Runs without the UI tag. */
    @ProbeTarget
    static final class NonUiTargetTest {
        @Test
        void executes() {
        }
    }

    /** Triggers migration validation through the UI tag. */
    @ProbeTarget
    @Tag("ui")
    static final class UiTargetTest {
        @Test
        void executes() {
        }
    }
}
