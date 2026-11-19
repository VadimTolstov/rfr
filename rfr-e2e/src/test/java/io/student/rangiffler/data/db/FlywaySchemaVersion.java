package io.student.rangiffler.data.db;

import java.util.Optional;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;

/** Reads migration history without borrowing connections from test fixture pools. */
public final class FlywaySchemaVersion implements SchemaVersion {
    private final String address;
    private final String username;
    private final String password;
    private final String schema;

    public FlywaySchemaVersion(String address, String username, String password, String schema) {
        this.address = address;
        this.username = username;
        this.password = password;
        this.schema = schema;
    }

    @Override
    public String value() {
        try {
            return Optional.ofNullable(Flyway.configure()
                            .dataSource(address + "/" + schema
                                    + "?serverTimezone=UTC&connectTimeout=1000&socketTimeout=1000", username, password)
                            .schemas(schema).connectRetries(0).load().info().current())
                    .map(MigrationInfo::getVersion)
                    .map(MigrationVersion::getVersion)
                    .orElse("none");
        } catch (FlywayException failure) {
            System.getLogger("io.student.rangiffler.allure").log(System.Logger.Level.WARNING,
                    "Migration version for {0} is unavailable", schema);
            return "unavailable";
        }
    }
}
