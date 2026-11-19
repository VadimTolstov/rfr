package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class StandReadinessExtension implements SuiteExtension {
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (context.getTags().contains("ui")
                && context.getConfigurationParameter("rfr.standCheck", Boolean::parseBoolean)
                        .orElseGet(TestConfig::standCheck)) {
            SuiteExtension.super.beforeAll(context);
        }
    }

    @Override
    public void beforeSuite(ExtensionContext context) {
        new StandReadiness(List.of(
                new StandEndpoint("MySQL", URI.create(setting(context, "dbUrl", TestConfig::dbUrl)
                        .replaceFirst("^jdbc:", "")), "docker compose up -d --wait", ""),
                new StandEndpoint("rfr-auth", URI.create(setting(context, "authUrl", TestConfig::authUrl)
                        + "/.well-known/openid-configuration"), "./gradlew runAll", ""),
                new StandEndpoint("rfr-api", URI.create(setting(context, "apiUrl", TestConfig::apiUrl)
                        + "/graphql"), "./gradlew runAll", "{\"query\":\"{__typename}\"}"),
                new StandEndpoint("rfr-gql-client", URI.create(setting(context, "frontUrl", TestConfig::frontUrl)),
                        "cd rfr-gql-client && npm run dev", "")),
                Duration.ofSeconds(Long.parseLong(setting(context, "standTimeout",
                        () -> Long.toString(TestConfig.standTimeout()))))).await();
    }

    private String setting(ExtensionContext context, String name, Supplier<String> fallback) {
        return context.getConfigurationParameter("rfr." + name).orElseGet(fallback);
    }
}
