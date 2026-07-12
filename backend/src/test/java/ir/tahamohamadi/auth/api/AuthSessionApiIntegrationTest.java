package ir.tahamohamadi.auth.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
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
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthSessionApiIntegrationTest {

    private static final UUID ADMIN_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SUPER_ADMIN_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String TEST_PASSWORD = "auth-session-integration-password";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("taha_auth_session_test")
            .withUsername("taha_test")
            .withPassword("taha_test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearApplicationUsersAndAuditEvents() {
        jdbcTemplate.update("DELETE FROM audit_event");
        jdbcTemplate.update("DELETE FROM user_role");
        jdbcTemplate.update("DELETE FROM app_user");
        jdbcTemplate.update("DELETE FROM role WHERE id NOT IN (?, ?)", ADMIN_ROLE_ID, SUPER_ADMIN_ROLE_ID);
    }

    @Test
    void bootstrapsTheCsrfCookieForTheJsonClient() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();

        assertThat(result.getResponse().getCookie("XSRF-TOKEN").isHttpOnly()).isFalse();
        assertThat(result.getResponse().getCookie("XSRF-TOKEN").getPath()).isEqualTo("/");
        assertThat(result.getResponse().getCookie("XSRF-TOKEN").getAttribute("SameSite")).isEqualTo("Lax");
    }

    @Test
    void rejectsLoginWithoutCsrf() throws Exception {
        mockMvc.perform(login("missing-csrf@example.test"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void rejectsLogoutWithoutCsrf() throws Exception {
        MockHttpSession authenticatedSession = loginSuccessfully(persistUser("logout-no-csrf@example.test"));

        mockMvc.perform(post("/api/v1/auth/logout").session(authenticatedSession))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void returnsTheSafeAuthenticatedUserAndNoStoreOnSuccessfulLogin() throws Exception {
        AppUser user = persistUser("safe-login@example.test");

        mockMvc.perform(login(user.getEmail()).with(validCsrf()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.displayName").value("Auth Session Test"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.sessionId").doesNotExist());
    }

    @Test
    void migratesThePreAuthenticationSessionOnSuccessfulLogin() throws Exception {
        AppUser user = persistUser("session-rotation@example.test");
        MockHttpSession preAuthenticationSession = new MockHttpSession();
        preAuthenticationSession.setAttribute("safe-attribute", "safe-value");
        String originalSessionId = preAuthenticationSession.getId();

        MvcResult result = mockMvc.perform(login(user.getEmail())
                        .session(preAuthenticationSession)
                        .with(validCsrf()))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession authenticatedSession = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(authenticatedSession).isNotNull();
        assertThat(authenticatedSession.getId()).isNotEqualTo(originalSessionId);
        assertThat(authenticatedSession.getAttribute("safe-attribute")).isEqualTo("safe-value");
    }

    @Test
    void returnsTheSameGenericFailureForAnUnknownUserAndWrongPassword() throws Exception {
        AppUser user = persistUser("wrong-password@example.test");

        JsonNode unknown = loginFailure("unknown@example.test", validCsrf());
        JsonNode wrongPassword = loginFailure(user.getEmail(), validCsrf(), "different-password");

        assertThat(unknown.path("status").asInt()).isEqualTo(wrongPassword.path("status").asInt()).isEqualTo(401);
        assertThat(unknown.path("error").asText()).isEqualTo(wrongPassword.path("error").asText()).isEqualTo("UNAUTHORIZED");
        assertThat(unknown.path("message").asText()).isEqualTo(wrongPassword.path("message").asText()).isEqualTo("Invalid credentials");
    }

    @Test
    void returnsTheSameGenericFailureForDisabledUsers() throws Exception {
        AppUser user = persistUser("disabled-login@example.test");
        jdbcTemplate.update("UPDATE app_user SET enabled = false WHERE id = ?", user.getId());

        assertGenericFailure(loginFailure(user.getEmail(), validCsrf()));
    }

    @Test
    void returnsTheSameGenericFailureForLockedUsers() throws Exception {
        AppUser user = persistUser("locked-login@example.test");
        jdbcTemplate.update(
                "UPDATE app_user SET locked_until = ? WHERE id = ?",
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5),
                user.getId()
        );

        assertGenericFailure(loginFailure(user.getEmail(), validCsrf()));
    }

    @Test
    void returnsTheSameGenericFailureForSoftDeletedUsers() throws Exception {
        AppUser user = persistUser("deleted-login@example.test");
        user.softDelete(null, Instant.now());
        appUserRepository.saveAndFlush(user);

        assertGenericFailure(loginFailure(user.getEmail(), validCsrf()));
    }

    @Test
    void incrementsTheFailedLoginCounterForKnownUsers() throws Exception {
        AppUser user = persistUser("failed-count@example.test");

        loginFailure(user.getEmail(), validCsrf(), "different-password");

        assertThat(appUserRepository.findById(user.getId()).orElseThrow().getFailedLoginCount()).isEqualTo(1);
    }

    @Test
    void resetsTheFailedLoginCounterAndSetsLastLoginAtAfterSuccess() throws Exception {
        AppUser user = persistUser("successful-state@example.test");
        loginFailure(user.getEmail(), validCsrf(), "different-password");
        Instant beforeSuccessfulLogin = Instant.now();

        loginSuccessfully(user);

        AppUser updated = appUserRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getFailedLoginCount()).isZero();
        assertThat(updated.getLastLoginAt()).isAfterOrEqualTo(beforeSuccessfulLogin);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(beforeSuccessfulLogin);
    }

    @Test
    void writesASanitizedSuccessfulLoginAuditEvent() throws Exception {
        AppUser user = persistUser("login-audit@example.test");

        loginSuccessfully(user);

        AuditEvent event = latestEvent("AUTH_LOGIN");
        assertThat(event.getActor().getId()).isEqualTo(user.getId());
        assertThat(event.getTargetId()).isEqualTo(user.getId());
        assertThat(event.getOutcome()).isEqualTo("SUCCESS");
        assertThat(event.getRequestId()).isNull();
        assertThat(event.getIpAddress()).isNull();
        assertSanitizedDetails(event, "PASSWORD");
    }

    @Test
    void writesASanitizedFailedLoginAuditEvent() throws Exception {
        AppUser user = persistUser("failed-audit@example.test");

        loginFailure(user.getEmail(), validCsrf(), "different-password");

        AuditEvent event = latestEvent("AUTH_LOGIN_FAILED");
        assertThat(event.getActor().getId()).isEqualTo(user.getId());
        assertThat(event.getTargetId()).isEqualTo(user.getId());
        assertThat(event.getOutcome()).isEqualTo("FAILURE");
        assertSanitizedDetails(event, "PASSWORD");
    }

    @Test
    void writesASanitizedFailedLoginAuditEventWithoutAnActorForUnknownUsers() throws Exception {
        loginFailure("unknown-audit@example.test", validCsrf());

        AuditEvent event = latestEvent("AUTH_LOGIN_FAILED");
        assertThat(event.getActor()).isNull();
        assertThat(event.getTargetId()).isNull();
        assertThat(event.getOutcome()).isEqualTo("FAILURE");
        assertSanitizedDetails(event, "PASSWORD");
    }

    @Test
    void returnsUnauthorizedForAnUnauthenticatedCurrentUserRequest() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void returnsTheCurrentAuthenticatedUserWithSortedActiveRoles() throws Exception {
        AppUser user = persistUser("current-user@example.test", true);
        MockHttpSession authenticatedSession = loginSuccessfully(user);

        mockMvc.perform(get("/api/v1/auth/me").session(authenticatedSession))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.displayName").value("Auth Session Test"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.roles[1]").value("SUPER_ADMIN"));
    }

    @Test
    void invalidatesTheSessionAndExpiresTheSessionCookieOnLogout() throws Exception {
        MockHttpSession authenticatedSession = loginSuccessfully(persistUser("logout@example.test"));

        mockMvc.perform(post("/api/v1/auth/logout").session(authenticatedSession).with(validCsrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(cookie().maxAge("JSESSIONID", 0))
                .andExpect(cookie().path("JSESSIONID", "/"))
                .andExpect(cookie().httpOnly("JSESSIONID", true));

        mockMvc.perform(get("/api/v1/auth/me").session(authenticatedSession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void writesASanitizedLogoutAuditEvent() throws Exception {
        AppUser user = persistUser("logout-audit@example.test");
        MockHttpSession authenticatedSession = loginSuccessfully(user);

        mockMvc.perform(post("/api/v1/auth/logout").session(authenticatedSession).with(validCsrf()))
                .andExpect(status().isNoContent());

        AuditEvent event = latestEvent("AUTH_LOGOUT");
        assertThat(event.getActor().getId()).isEqualTo(user.getId());
        assertThat(event.getTargetId()).isEqualTo(user.getId());
        assertThat(event.getOutcome()).isEqualTo("SUCCESS");
        assertSanitizedDetails(event, "SESSION");
    }

    @Test
    void returnsScopedValidationErrorsWithoutRejectedValues() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"\"}")
                        .with(validCsrf()))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid login request"))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[*].rejectedValue").doesNotExist());
    }

    private AppUser persistUser(String email) {
        return persistUser(email, false);
    }

    private AppUser persistUser(String email, boolean includeSuperAdminRole) {
        AppUser user = appUserRepository.saveAndFlush(AppUser.create(
                email,
                passwordEncoder.encode(TEST_PASSWORD),
                "Auth Session Test",
                Instant.now()
        ));
        Role admin = roleRepository.findById(ADMIN_ROLE_ID).orElseThrow();
        userRoleRepository.saveAndFlush(UserRole.assign(user, admin, null, Instant.now()));
        if (includeSuperAdminRole) {
            Role superAdmin = roleRepository.findById(SUPER_ADMIN_ROLE_ID).orElseThrow();
            userRoleRepository.saveAndFlush(UserRole.assign(user, superAdmin, null, Instant.now()));
        }
        return user;
    }

    private MockHttpSession loginSuccessfully(AppUser user) throws Exception {
        MvcResult result = mockMvc.perform(login(user.getEmail()).with(validCsrf()))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private JsonNode loginFailure(String email, org.springframework.test.web.servlet.request.RequestPostProcessor csrf)
            throws Exception {
        return loginFailure(email, csrf, "different-password");
    }

    private JsonNode loginFailure(
            String email,
            org.springframework.test.web.servlet.request.RequestPostProcessor csrf,
            String password
    ) throws Exception {
        MvcResult result = mockMvc.perform(login(email, password).with(csrf))
                .andExpect(status().isUnauthorized())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor validCsrf() throws Exception {
        MvcResult bootstrap = mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isNoContent())
                .andReturn();
        jakarta.servlet.http.Cookie csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
        return request -> {
            request.setCookies(csrfCookie);
            request.addHeader("X-XSRF-TOKEN", csrfCookie.getValue());
            return request;
        };
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder login(String email) throws Exception {
        return login(email, TEST_PASSWORD);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder login(String email, String password)
            throws Exception {
        return post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginPayload(email, password)));
    }

    private AuditEvent latestEvent(String action) {
        List<AuditEvent> events = auditEventRepository.findAll().stream()
                .filter(event -> event.getAction().equals(action))
                .toList();
        assertThat(events).hasSize(1);
        return events.getFirst();
    }

    private void assertGenericFailure(JsonNode response) {
        assertThat(response.path("status").asInt()).isEqualTo(401);
        assertThat(response.path("error").asText()).isEqualTo("UNAUTHORIZED");
        assertThat(response.path("message").asText()).isEqualTo("Invalid credentials");
    }

    private void assertSanitizedDetails(AuditEvent event, String authMethod) {
        assertThat(event.getDetails().fieldNames()).toIterable().containsExactly("auth_method");
        assertThat(event.getDetails().path("auth_method").asText()).isEqualTo(authMethod);
    }

    private record LoginPayload(String email, String password) {
    }
}
