package ir.tahamohamadi.auth.qa;

import ir.tahamohamadi.identity.assignment.UserRoleRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "TAHA_QA_ADMIN_EMAIL=qa-admin@example.test",
        "TAHA_QA_ADMIN_PASSWORD=qa-bootstrap-test-password"
})
@ActiveProfiles("qa")
class QaAdminBootstrapIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("taha_qa_bootstrap_test")
            .withUsername("taha_test")
            .withPassword("taha_test");

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    void createsTheConfiguredQaAdministratorWithTheAdminRole() {
        var user = appUserRepository.findByNormalizedEmail("qa-admin@example.test");

        assertThat(user).isPresent();
        assertThat(user.orElseThrow().getDisplayName()).isEqualTo("QA Admin");
        assertThat(userRoleRepository.findActiveRoleCodesByUserId(user.orElseThrow().getId()))
                .containsExactly("ADMIN");
    }
}
