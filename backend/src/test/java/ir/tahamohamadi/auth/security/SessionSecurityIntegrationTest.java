package ir.tahamohamadi.auth.security;

import ir.tahamohamadi.identity.assignment.UserRole;
import ir.tahamohamadi.identity.assignment.UserRoleRepository;
import ir.tahamohamadi.identity.role.Role;
import ir.tahamohamadi.identity.role.RoleRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Import(SessionSecurityIntegrationTest.TestAdminController.class)
class SessionSecurityIntegrationTest {

    private static final UUID ADMIN_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SUPER_ADMIN_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String TEST_PASSWORD = "integration-test-password";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("taha_security_test")
            .withUsername("taha_test")
            .withPassword("taha_test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PersistedUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearApplicationUsersAndNonSystemRoles() {
        jdbcTemplate.update("DELETE FROM user_role");
        jdbcTemplate.update("DELETE FROM app_user");
        jdbcTemplate.update("DELETE FROM role WHERE id NOT IN (?, ?)", ADMIN_ROLE_ID, SUPER_ADMIN_ROLE_ID);
    }

    @Test
    void rejectsUnauthenticatedAdminRequestsWithSafeJson() throws Exception {
        mockMvc.perform(get("/api/v1/admin/security-test"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(content().json("{" +
                        "\"status\":401," +
                        "\"error\":\"UNAUTHORIZED\"," +
                        "\"message\":\"Authentication is required\"," +
                        "\"path\":\"/api/v1/admin/security-test\"" +
                        "}"));
    }

    @Test
    void enforcesAdminAuthoritiesOnAdminRoutes() throws Exception {
        mockMvc.perform(get("/api/v1/admin/security-test").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/json"));

        mockMvc.perform(get("/api/v1/admin/security-test").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/security-test").with(user("owner").roles("SUPER_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void loadsOnlyActivePersistedRoleCodesForNormalizedEmail() {
        AppUser appUser = persistUser("Case.User@example.test");
        Role admin = roleRepository.findById(ADMIN_ROLE_ID).orElseThrow();
        userRoleRepository.saveAndFlush(UserRole.assign(appUser, admin, null, Instant.now()));
        UUID inactiveRoleId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        jdbcTemplate.update(
                "INSERT INTO role (id, code, is_active, created_at, updated_at, version) VALUES (?, ?, ?, ?, ?, ?)",
                inactiveRoleId, "INACTIVE_TEST", false, now, now, 0L
        );
        jdbcTemplate.update(
                "INSERT INTO user_role (user_id, role_id, assigned_at) VALUES (?, ?, ?)",
                appUser.getId(), inactiveRoleId, now
        );

        UserDetails details = userDetailsService.loadUserByUsername("  case.user@EXAMPLE.test  ");

        assertThat(details.getAuthorities()).extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void rejectsDisabledLockedAndSoftDeletedUsers() {
        AppUser disabled = persistUser("disabled@example.test");
        jdbcTemplate.update("UPDATE app_user SET enabled = false WHERE id = ?", disabled.getId());
        assertThatThrownBy(() -> authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(disabled.getEmail(), TEST_PASSWORD)
        )).isInstanceOf(DisabledException.class);

        AppUser locked = persistUser("locked@example.test");
        jdbcTemplate.update(
                "UPDATE app_user SET locked_until = ? WHERE id = ?",
                OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(300),
                locked.getId()
        );
        assertThatThrownBy(() -> authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(locked.getEmail(), TEST_PASSWORD)
        )).isInstanceOf(LockedException.class);

        AppUser deleted = persistUser("deleted@example.test");
        deleted.softDelete(null, Instant.now());
        appUserRepository.saveAndFlush(deleted);
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(deleted.getEmail()))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void deliversCsrfCookieAndEnforcesCsrfForUnsafeAdminRequests() throws Exception {
        MvcResult bootstrap = mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();

        assertThat(bootstrap.getResponse().getCookie("XSRF-TOKEN").isHttpOnly()).isFalse();
        assertThat(bootstrap.getResponse().getCookie("XSRF-TOKEN").getPath()).isEqualTo("/");
        assertThat(bootstrap.getResponse().getCookie("XSRF-TOKEN").getAttribute("SameSite")).isEqualTo("Lax");
        assertThat(bootstrap.getResponse().getCookie("XSRF-TOKEN").getSecure()).isTrue();

        mockMvc.perform(post("/api/v1/admin/security-test").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/json"));

        mockMvc.perform(post("/api/v1/admin/security-test")
                        .with(user("admin").roles("ADMIN"))
                        .cookie(bootstrap.getResponse().getCookie("XSRF-TOKEN"))
                        .header("X-XSRF-TOKEN", "invalid-token"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/admin/security-test")
                        .with(user("admin").roles("ADMIN"))
                        .cookie(bootstrap.getResponse().getCookie("XSRF-TOKEN"))
                        .header("X-XSRF-TOKEN", bootstrap.getResponse().getCookie("XSRF-TOKEN").getValue()))
                .andExpect(status().isOk());
    }

    @Test
    void doesNotEmitCorsHeadersForArbitraryOrigins() throws Exception {
        mockMvc.perform(get("/api/v1/admin/security-test")
                        .header(HttpHeaders.ORIGIN, "https://untrusted.example"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    private AppUser persistUser(String email) {
        return appUserRepository.saveAndFlush(AppUser.create(
                email,
                passwordEncoder.encode(TEST_PASSWORD),
                "Security Test",
                Instant.now()
        ));
    }

    @TestConfiguration(proxyBeanMethods = false)
    @RestController
    @RequestMapping("/api/v1/admin/security-test")
    static class TestAdminController {

        @GetMapping
        ResponseEntity<Void> get() {
            return ResponseEntity.ok().build();
        }

        @PostMapping
        ResponseEntity<Void> post() {
            return ResponseEntity.ok().build();
        }
    }
}
