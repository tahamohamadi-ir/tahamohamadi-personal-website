package ir.tahamohamadi.auth.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigurationUnitTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void encodesAndMatchesPasswordsUsingConfiguredBcryptStrength() {
        AuthSecurityProperties properties = new AuthSecurityProperties();
        properties.setBcryptStrength(4);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(properties.getBcryptStrength());

        String hash = encoder.encode("unit-test-password");

        assertThat(encoder.matches("unit-test-password", hash)).isTrue();
        assertThat(encoder.matches("different-unit-test-password", hash)).isFalse();
        assertThat(hash).doesNotContain("unit-test-password");
    }

    @Test
    void writesSafeJsonForUnauthenticatedRequests() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/posts");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JsonAuthenticationEntryPoint(objectMapper).commence(request, response, null);

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store");
        assertThat(body.fieldNames()).toIterable()
                .containsExactlyInAnyOrder("timestamp", "status", "error", "message", "path");
        assertThat(body.path("path").asText()).isEqualTo("/api/v1/admin/posts");
        assertThat(response.getContentAsString()).doesNotContain("password", "session", "cookie", "csrf");
    }

    @Test
    void writesSafeJsonForForbiddenRequestsWithoutExceptionDetails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/posts");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JsonAccessDeniedHandler(objectMapper).handle(
                request,
                response,
                new org.springframework.security.access.AccessDeniedException("sensitive internal detail")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store");
        assertThat(body.fieldNames()).toIterable()
                .containsExactlyInAnyOrder("timestamp", "status", "error", "message", "path");
        assertThat(response.getContentAsString()).doesNotContain("sensitive internal detail");
    }

    @Test
    void migratesThePreAuthenticationSessionIdentifier() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("safe-attribute", "safe-value");
        String originalSessionId = session.getId();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);

        new SessionFixationProtectionStrategy().onAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        "admin@example.test",
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                ),
                request,
                new MockHttpServletResponse()
        );

        assertThat(request.getSession(false)).isNotNull();
        assertThat(request.getSession(false).getId()).isNotEqualTo(originalSessionId);
        assertThat(request.getSession(false).getAttribute("safe-attribute")).isEqualTo("safe-value");
    }

    @Test
    void defaultsToProductionSafeCookieSettingsAndAllowsLocalSecureOverride() {
        AuthSecurityProperties properties = new AuthSecurityProperties();

        assertThat(properties.getBcryptStrength()).isEqualTo(12);
        assertThat(properties.isSecureCookies()).isTrue();

        properties.setSecureCookies(false);

        assertThat(properties.isSecureCookies()).isFalse();
    }
}
