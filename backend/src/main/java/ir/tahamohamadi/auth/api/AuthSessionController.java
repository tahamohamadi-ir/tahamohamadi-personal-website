package ir.tahamohamadi.auth.api;

import ir.tahamohamadi.auth.application.AuthSessionService;
import ir.tahamohamadi.auth.application.AuthenticationAttemptResult;
import ir.tahamohamadi.auth.security.AuthSecurityProperties;
import ir.tahamohamadi.auth.security.SecurityErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthSessionController {

    private final AuthSessionService authSessionService;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final AuthSecurityProperties securityProperties;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthenticationAttemptResult result = authSessionService.authenticate(request);
        if (!result.isSuccessful()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .body(new SecurityErrorResponse(
                            Instant.now().toString(),
                            HttpStatus.UNAUTHORIZED.value(),
                            "UNAUTHORIZED",
                            "Invalid credentials",
                            httpRequest.getRequestURI()
                    ));
        }

        sessionAuthenticationStrategy.onAuthentication(result.authentication(), httpRequest, httpResponse);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(result.authentication());
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(result.authenticatedUser());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        authSessionService.recordLogout(authentication.getName());
        SecurityContextHolder.clearContext();
        securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .maxAge(Duration.ZERO)
                .httpOnly(true)
                .secure(securityProperties.isSecureCookies())
                .sameSite("Lax")
                .build()
                .toString());
        return ResponseEntity.noContent().header(HttpHeaders.CACHE_CONTROL, "no-store").build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> currentUser(Authentication authentication) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(authSessionService.currentAuthenticatedUser(authentication.getName()));
    }
}
