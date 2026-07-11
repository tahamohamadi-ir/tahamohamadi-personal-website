package ir.tahamohamadi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.identity.assignment.UserRole;
import ir.tahamohamadi.identity.assignment.UserRoleId;
import ir.tahamohamadi.identity.assignment.UserRoleRepository;
import ir.tahamohamadi.identity.role.Role;
import ir.tahamohamadi.identity.role.RoleRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import jakarta.persistence.EntityManager;
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

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
    private static final Instant TEST_TIME = Instant.parse("2026-01-02T03:04:05Z");

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

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
        assertThat(jdbcTemplate.queryForObject("SELECT version()", String.class)).contains("PostgreSQL 17");
    }

    @Test
    void persistsAndReloadsAnAppUser() {
        AppUser user = newUser("persist-reload", "persist-reload@example.test");

        appUserRepository.saveAndFlush(user);
        entityManager.clear();

        AppUser reloaded = appUserRepository.findById(user.getId()).orElseThrow();
        assertThat(reloaded.getEmail()).isEqualTo("persist-reload@example.test");
        assertThat(reloaded.getDisplayName()).isEqualTo("Persistence Test");
        assertThat(reloaded.isEnabled()).isTrue();
        assertThat(reloaded.getFailedLoginCount()).isZero();
    }

    @Test
    void assignsAppUserUuidBeforePersistence() {
        AppUser user = newUser("application-uuid", "application-uuid@example.test");

        assertThat(user.getId()).isNotNull();
        appUserRepository.saveAndFlush(user);

        assertThat(appUserRepository.findById(user.getId())).isPresent();
    }

    @Test
    void initializesAndUpdatesTheOptimisticVersion() {
        AppUser user = appUserRepository.saveAndFlush(newUser("versioned", "versioned@example.test"));
        assertThat(user.getVersion()).isZero();

        user.rename("Renamed Persistence Test");
        AppUser updated = appUserRepository.saveAndFlush(user);

        assertThat(updated.getVersion()).isEqualTo(1L);
    }

    @Test
    void findsANondeletedUserByNormalizedCaseInsensitiveEmail() {
        AppUser user = appUserRepository.saveAndFlush(newUser("case-insensitive", "Case.User@example.test"));

        assertThat(appUserRepository.findByNormalizedEmail("  case.user@EXAMPLE.test  "))
                .map(AppUser::getId)
                .contains(user.getId());
        assertThat(appUserRepository.existsByNormalizedEmail("CASE.USER@example.test")).isTrue();
    }

    @Test
    void excludesSoftDeletedUsersFromNormalEmailLookup() {
        AppUser user = appUserRepository.saveAndFlush(newUser("soft-deleted", "soft-deleted@example.test"));
        user.softDelete(null, TEST_TIME.plusSeconds(1));
        appUserRepository.saveAndFlush(user);

        assertThat(appUserRepository.findByNormalizedEmail("soft-deleted@example.test")).isEmpty();
        assertThat(appUserRepository.existsByNormalizedEmail("soft-deleted@example.test")).isFalse();
    }

    @Test
    void permitsEmailReuseAfterSoftDeletion() {
        AppUser deletedUser = appUserRepository.saveAndFlush(newUser("reused-deleted", "reused@example.test"));
        deletedUser.softDelete(null, TEST_TIME.plusSeconds(1));
        appUserRepository.saveAndFlush(deletedUser);

        AppUser reusedUser = appUserRepository.saveAndFlush(newUser("reused-active", "REUSED@example.test"));

        assertThat(appUserRepository.findByNormalizedEmail("reused@example.test"))
                .map(AppUser::getId)
                .contains(reusedUser.getId());
    }

    @Test
    void mapsAndLoadsSeededActiveRoles() {
        Role admin = roleRepository.findActiveByCode("ADMIN").orElseThrow();
        Role superAdmin = roleRepository.findActiveByCode("SUPER_ADMIN").orElseThrow();

        assertThat(admin.getId()).isEqualTo(ADMIN_ROLE_ID);
        assertThat(superAdmin.getId()).isEqualTo(SUPER_ADMIN_ROLE_ID);
        assertThat(admin.isActive()).isTrue();
    }

    @Test
    void persistsAndLoadsAUserRoleWithItsCompositeIdentifier() {
        AppUser user = appUserRepository.saveAndFlush(newUser("user-role", "user-role@example.test"));
        Role role = roleRepository.findActiveByCode("ADMIN").orElseThrow();
        UserRole assignment = UserRole.assign(user, role, null, TEST_TIME);

        userRoleRepository.saveAndFlush(assignment);
        entityManager.clear();

        UserRole reloaded = userRoleRepository.findById(new UserRoleId(user.getId(), role.getId())).orElseThrow();
        assertThat(reloaded.getAssignedAt()).isEqualTo(TEST_TIME);
        assertThat(userRoleRepository.findByUserId(user.getId())).extracting(UserRole::getId)
                .containsExactly(new UserRoleId(user.getId(), role.getId()));
    }

    @Test
    void rejectsDuplicateUserRoleAssignmentsThroughTheDatabaseConstraint() {
        AppUser user = appUserRepository.saveAndFlush(newUser("duplicate-user-role", "duplicate-user-role@example.test"));
        Role role = roleRepository.findActiveByCode("ADMIN").orElseThrow();
        userRoleRepository.saveAndFlush(UserRole.assign(user, role, null, TEST_TIME));

        assertThatThrownBy(() -> assignRole(user.getId(), role.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void removingAUserRoleDoesNotDeleteItsUserOrRole() {
        AppUser user = appUserRepository.saveAndFlush(newUser("remove-user-role", "remove-user-role@example.test"));
        Role role = roleRepository.findActiveByCode("ADMIN").orElseThrow();
        UserRole assignment = userRoleRepository.saveAndFlush(UserRole.assign(user, role, null, TEST_TIME));

        userRoleRepository.delete(assignment);
        userRoleRepository.flush();

        assertThat(userRoleRepository.existsById(assignment.getId())).isFalse();
        assertThat(appUserRepository.existsById(user.getId())).isTrue();
        assertThat(roleRepository.existsById(role.getId())).isTrue();
    }

    @Test
    void persistsAndReloadsAnAuditEventWithObjectJsonDetails() throws Exception {
        AppUser actor = appUserRepository.saveAndFlush(newUser("audit-actor", "audit-actor@example.test"));
        ObjectNode details = objectMapper.createObjectNode().put("source", "persistence-test");
        AuditEvent event = AuditEvent.record(
                uuidFor("audit-json"),
                TEST_TIME,
                actor,
                "USER_LOGIN",
                "app_user",
                actor.getId(),
                "SUCCESS",
                "request-audit-json",
                InetAddress.getByName("203.0.113.10"),
                details
        );

        auditEventRepository.saveAndFlush(event);
        entityManager.clear();

        AuditEvent reloaded = auditEventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getDetails().path("source").asText()).isEqualTo("persistence-test");
        assertThat(reloaded.getIpAddress()).isEqualTo(InetAddress.getByName("203.0.113.10"));
    }

    @Test
    void leavesNonObjectAuditDetailsRejectedByTheDatabase() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                """
                        INSERT INTO audit_event (id, occurred_at, action, target_type, outcome, details)
                        VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb))
                        """,
                uuidFor("invalid-audit-details"), OffsetDateTime.ofInstant(TEST_TIME, ZoneOffset.UTC),
                "USER_LOGIN", "app_user", "SUCCESS", "[\"invalid\"]"
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void retrievesAuditEventsByActorInDescendingOccurrenceOrder() {
        AppUser actor = appUserRepository.saveAndFlush(newUser("ordered-audit-actor", "ordered-audit-actor@example.test"));
        AuditEvent older = auditEventRepository.saveAndFlush(newAuditEvent("older-audit-event", actor, TEST_TIME));
        AuditEvent newer = auditEventRepository.saveAndFlush(newAuditEvent("newer-audit-event", actor, TEST_TIME.plusSeconds(1)));

        assertThat(auditEventRepository.findByActorIdOrderByOccurredAtDesc(actor.getId()))
                .extracting(AuditEvent::getId)
                .containsExactly(newer.getId(), older.getId());
    }

    @Test
    void omitsPasswordHashFromAppUserToString() {
        AppUser user = AppUser.create(
                uuidFor("to-string"),
                "to-string@example.test",
                "sensitive-password-hash",
                "Persistence Test",
                TEST_TIME
        );

        assertThat(user).doesNotHaveToString("sensitive-password-hash");
    }

    @Test
    void basesEntityEqualityOnIdentifiersWithoutTraversingAssociations() {
        AppUser user = newUser("equality-user", "equality-user@example.test");
        Role role = roleRepository.findActiveByCode("ADMIN").orElseThrow();
        UserRole first = UserRole.assign(user, role, null, TEST_TIME);
        UserRole second = UserRole.assign(user, role, null, TEST_TIME.plusSeconds(1));

        assertThat(first).isEqualTo(second);
        assertThat(first.toString()).doesNotContain("user=", "role=");
    }

    private AppUser newUser(String idSeed, String email) {
        return AppUser.create(
                uuidFor(idSeed),
                email,
                "test-password-hash-" + idSeed,
                "Persistence Test",
                TEST_TIME
        );
    }

    private AuditEvent newAuditEvent(String idSeed, AppUser actor, Instant occurredAt) {
        return AuditEvent.record(
                uuidFor(idSeed),
                occurredAt,
                actor,
                "USER_LOGIN",
                "app_user",
                actor.getId(),
                "SUCCESS",
                "request-" + idSeed,
                null,
                objectMapper.createObjectNode().put("seed", idSeed)
        );
    }

    private UUID uuidFor(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
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
