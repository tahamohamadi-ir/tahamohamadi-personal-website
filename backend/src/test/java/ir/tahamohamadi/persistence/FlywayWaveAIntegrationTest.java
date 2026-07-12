package ir.tahamohamadi.persistence;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Testcontainers
class FlywayWaveAIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("taha_wave_a_flyway")
            .withUsername("taha_test")
            .withPassword("taha_test");

    @Test
    void migratesFreshPostgreSqlToWaveAAndThenIsANoOp() throws Exception {
        Flyway flyway = flyway();

        MigrateResult firstMigration = flyway.migrate();

        assertThat(firstMigration.targetSchemaVersion).isEqualTo("4");
        assertThat(tableNames()).containsExactlyInAnyOrder(
                "app_user", "audit_event", "blog_category", "blog_category_translation", "blog_post",
                "blog_post_media", "blog_post_tag", "blog_post_translation", "contact_message",
                "content_page", "content_page_translation", "media_asset", "media_asset_translation", "role",
                "social_link", "tag", "tag_translation", "user_role"
        );
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThatCode(flyway::validate).doesNotThrowAnyException();
    }

    private Flyway flyway() {
        return Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .target("4")
                .load();
    }

    private List<String> tableNames() throws Exception {
        try (Connection connection = POSTGRES.createConnection("");
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     SELECT table_name
                     FROM information_schema.tables
                     WHERE table_schema = 'public'
                       AND table_type = 'BASE TABLE'
                       AND table_name <> 'flyway_schema_history'
                     ORDER BY table_name
                     """)) {
            java.util.ArrayList<String> names = new java.util.ArrayList<>();
            while (result.next()) {
                names.add(result.getString(1));
            }
            return names;
        }
    }
}
