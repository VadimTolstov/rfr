package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Migrations;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class StandSchemaExtension implements SuiteExtension {
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (context.getTags().contains("ui")
                && context.getConfigurationParameter("rfr.migrationCheck", Boolean::parseBoolean)
                        .orElseGet(TestConfig::migrationCheck)) {
            SuiteExtension.super.beforeAll(context);
        }
    }

    @Override
    public void beforeSuite(ExtensionContext context) {
        List<String> mismatches = Stream.of(
                        new Migrations(Databases.AUTH, context.getConfigurationParameter("rfr.authMigrations")
                                .orElseGet(TestConfig::authMigrations)),
                        new Migrations(Databases.API, context.getConfigurationParameter("rfr.apiMigrations")
                                .orElseGet(TestConfig::apiMigrations)))
                .map(Migrations::mismatch)
                .flatMap(Optional::stream)
                .toList();
        if (!mismatches.isEmpty()) {
            throw new IllegalStateException(
                    "Local database schema does not match the backend migrations of the current branch, "
                            + "restart the backends with './gradlew runAll' so Flyway applies pending migrations, "
                            + "and if the mismatch remains recreate the database with "
                            + "'docker compose down -v && docker compose up -d --wait' and restart the backends again"
                            + System.lineSeparator()
                            + String.join(System.lineSeparator(), mismatches));
        }
    }
}
