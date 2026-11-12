package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.FlywaySchemaVersion;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Supplier;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Publishes run metadata when the Jupiter root store closes, including failed setup runs. */
public final class AllureEnvironmentExtension implements SuiteExtension {
    @Override
    public void afterSuite(ExtensionContext context) {
        Properties environment = new Properties();
        environment.setProperty("frontUrl", setting(context, "frontUrl", TestConfig::frontUrl));
        environment.setProperty("authUrl", setting(context, "authUrl", TestConfig::authUrl));
        environment.setProperty("apiUrl", setting(context, "apiUrl", TestConfig::apiUrl));
        environment.setProperty("browser", setting(context, "browser", TestConfig::browser));
        environment.setProperty("headless", setting(context, "headless", () -> Boolean.toString(TestConfig.headless())));
        environment.setProperty("commit", setting(context, "commit", TestConfig::commit));
        for (Databases database : Databases.values()) {
            environment.setProperty("migration." + database.schema(),
                    version(context, database));
        }
        Path directory = Path.of(context.getConfigurationParameter("allure.results.directory")
                .orElseGet(() -> System.getProperty("allure.results.directory", "rfr-e2e/build/allure-results")));
        try {
            Files.createDirectories(directory);
            try (var output = Files.newBufferedWriter(directory.resolve("environment.properties"), StandardCharsets.UTF_8)) {
                environment.store(output, "Run environment");
            }
        } catch (IOException failure) {
            throw new UncheckedIOException("Cannot write Allure environment in " + directory, failure);
        }
    }

    private String version(ExtensionContext context, Databases database) {
        return Boolean.parseBoolean(setting(context, "environmentDb", () -> Boolean.toString(TestConfig.environmentDb())))
                ? new FlywaySchemaVersion(setting(context, "dbUrl", TestConfig::dbUrl),
                        setting(context, "dbUser", TestConfig::dbUser),
                        setting(context, "dbPassword", TestConfig::dbPassword), database.schema()).value()
                : "unavailable";
    }
    private String setting(ExtensionContext context, String name, Supplier<String> fallback) {
        return context.getConfigurationParameter("rfr." + name).orElseGet(fallback);
    }
}
