package ir.tahamohamadi.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class FlywayWaveBIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Test
    void migratesEveryAvailableMigrationAndThenIsANoOp() {
        Flyway flyway = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load();

        assertThat(flyway.migrate().migrationsExecuted).isPositive();
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.migrate().migrationsExecuted).isZero();
    }
}
