package ir.tahamohamadi;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.jpa.hibernate.ddl-auto=validate"
)
class FlywayV1IdentityAuditIntegrationTest {

    private static final UUID ADMIN_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SUPER_ADMIN_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("taha_v1_test")
            .withUsername("taha_test")
            .withPassword("taha_test");

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void appliesV1ToAFreshPostgreSqlDatabase() {
        Integer successfulV1Rows = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE version = '1' AND success",
                Integer.class
        );

        assertThat(successfulV1Rows).isEqualTo(1);
    }

    @Test
    void recordsOnlyTheExpectedV1ApplicationTables() {
        List<String> applicationTables = jdbcTemplate.queryForList(
                """
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_type = 'BASE TABLE'
                          AND table_name <> 'flyway_schema_history'
                        ORDER BY table_name
                        """,
                String.class
        );

        assertThat(applicationTables).containsExactly("app_user", "audit_event", "role", "user_role");
    }

    @Test
    void doesNotRunAdditionalMigrationsWhenMigrateIsCalledAgain() {
        MigrateResult result = flyway.migrate();

        assertThat(result.migrationsExecuted).isZero();
    }

    @Test
    void validatesTheAppliedSchema() {
        assertThatCode(flyway::validate).doesNotThrowAnyException();
    }

    @Test
    @Order(1)
    void seedsOnlyTheDocumentedSystemRoles() {
        List<UUID> roleIds = jdbcTemplate.queryForList(
                "SELECT id FROM role WHERE code IN ('ADMIN', 'SUPER_ADMIN') ORDER BY code",
                UUID.class
        );
        Integer userCount = jdbcTemplate.queryForObject("SELECT count(*) FROM app_user", Integer.class);

        assertThat(roleIds).containsExactly(ADMIN_ROLE_ID, SUPER_ADMIN_ROLE_ID);
        assertThat(userCount).isZero();
    }

    @Test
    void rejectsCaseInsensitiveDuplicateEmailsForNondeletedUsers() {
        insertUser(UUID.randomUUID(), "admin@example.test", null);

        assertThatThrownBy(() -> insertUser(UUID.randomUUID(), "ADMIN@example.test", null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsReusingAnEmailAfterSoftDeletion() {
        insertUser(UUID.randomUUID(), "deleted@example.test", OffsetDateTime.now(ZoneOffset.UTC));

        insertUser(UUID.randomUUID(), "DELETED@example.test", null);

        Integer activeUserCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM app_user WHERE lower(email) = lower(?) AND deleted_at IS NULL",
                Integer.class,
                "deleted@example.test"
        );
        assertThat(activeUserCount).isEqualTo(1);
    }

    @Test
    void rejectsNegativeFailedLoginCounts() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                """
                        INSERT INTO app_user (
                            id, email, password_hash, display_name, enabled, failed_login_count,
                            created_at, updated_at, version
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                UUID.randomUUID(), "negative-count@example.test", "hash", "Test User", true, -1,
                OffsetDateTime.now(ZoneOffset.UTC), OffsetDateTime.now(ZoneOffset.UTC), 0L
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsLowercaseRoleCodes() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                """
                        INSERT INTO role (id, code, is_active, created_at, updated_at, version)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                UUID.randomUUID(), "admin", true,
                OffsetDateTime.now(ZoneOffset.UTC), OffsetDateTime.now(ZoneOffset.UTC), 0L
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateUserRoleAssignments() {
        UUID userId = UUID.randomUUID();
        insertUser(userId, "role-assignment@example.test", null);
        assignRole(userId, ADMIN_ROLE_ID);

        assertThatThrownBy(() -> assignRole(userId, ADMIN_ROLE_ID))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsInvalidAuditOutcomes() {
        assertThatThrownBy(() -> insertAuditEvent("NOT_AN_OUTCOME", "{}"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsNonObjectAuditDetails() {
        assertThatThrownBy(() -> insertAuditEvent("SUCCESS", "[\"invalid\"]"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void acceptsAValidAuditEvent() {
        UUID eventId = UUID.randomUUID();

        jdbcTemplate.update(
                """
                        INSERT INTO audit_event (
                            id, occurred_at, action, target_type, outcome, details
                        ) VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb))
                        """,
                eventId, OffsetDateTime.now(ZoneOffset.UTC), "USER_LOGIN", "app_user", "SUCCESS",
                "{\"source\":\"integration-test\"}"
        );

        Integer eventCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM audit_event WHERE id = ?",
                Integer.class,
                eventId
        );
        assertThat(eventCount).isEqualTo(1);
    }

    @Test
    void startsHibernateValidationAgainstTheMigratedSchema() {
        assertThat(entityManagerFactory).isNotNull();
    }

    private void insertUser(UUID id, String email, OffsetDateTime deletedAt) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        jdbcTemplate.update(
                """
                        INSERT INTO app_user (
                            id, email, password_hash, display_name, enabled, failed_login_count,
                            created_at, updated_at, deleted_at, version
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id, email, "hash", "Test User", true, 0, now, now, deletedAt, 0L
        );
    }

    private void assignRole(UUID userId, UUID roleId) {
        jdbcTemplate.update(
                "INSERT INTO user_role (user_id, role_id, assigned_at) VALUES (?, ?, ?)",
                userId, roleId, OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    private void insertAuditEvent(String outcome, String details) {
        jdbcTemplate.update(
                """
                        INSERT INTO audit_event (
                            id, occurred_at, action, target_type, outcome, details
                        ) VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb))
                        """,
                UUID.randomUUID(), OffsetDateTime.now(ZoneOffset.UTC), "USER_LOGIN", "app_user", outcome, details
        );
    }
}
