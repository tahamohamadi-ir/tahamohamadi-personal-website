package ir.tahamohamadi.persistence;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Testcontainers
class FlywayUpgradeIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("taha_upgrade")
            .withUsername("taha_test")
            .withPassword("taha_test");

    @Test
    void upgradesV4DatabaseToV7WithoutLosingExistingDataAndThenIsANoOp() throws Exception {
        Flyway v1 = configuredFlyway().target("1").load();
        v1.migrate();
        Integer v1Checksum = checksumFor("1");

        Flyway current = configuredFlyway().target("4").load();
        assertThat(current.migrate().migrationsExecuted).isEqualTo(3);
        UUID pageId = UUID.randomUUID();
        try (Connection connection = POSTGRES.createConnection("");
             Statement statement = connection.createStatement()) {
            Instant now = Instant.parse("2026-01-01T00:00:00Z");
            statement.executeUpdate("""
                    INSERT INTO content_page (id, page_key, status, published_at, created_at, updated_at, version)
                    VALUES ('%s', 'upgrade-page', 'PUBLISHED', '%s', '%s', '%s', 0)
                    """.formatted(pageId, now, now, now));
        }

        Flyway waveB = configuredFlyway().target("7").load();
        assertThat(waveB.migrate().migrationsExecuted).isEqualTo(3);

        assertThat(checksumFor("1")).isEqualTo(v1Checksum);
        assertThat(waveB.info().current().getVersion().getVersion()).isEqualTo("7");
        assertThat(tableExists("portfolio_project")).isTrue();
        assertThat(tableExists("publication")).isTrue();
        assertThat(tableExists("featured_item")).isTrue();
        assertThat(waveB.migrate().migrationsExecuted).isZero();
        assertThatCode(waveB::validate).doesNotThrowAnyException();
    }

    private FluentConfiguration configuredFlyway() {
        return Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration");
    }

    private Integer checksumFor(String version) throws Exception {
        try (Connection connection = POSTGRES.createConnection("");
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     SELECT checksum
                     FROM flyway_schema_history
                     WHERE version = '%s' AND success
                     """.formatted(version))) {
            assertThat(result.next()).isTrue();
            return result.getInt(1);
        }
    }

    private boolean tableExists(String table) throws Exception {
        try (Connection connection = POSTGRES.createConnection("");
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT to_regclass('public.%s')".formatted(table))) {
            return result.next() && result.getString(1) != null;
        }
    }
}
