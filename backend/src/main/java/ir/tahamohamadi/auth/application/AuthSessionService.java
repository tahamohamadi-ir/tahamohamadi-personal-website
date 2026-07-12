package ir.tahamohamadi.auth.application;

import ir.tahamohamadi.auth.api.AuthenticatedUserResponse;
import ir.tahamohamadi.auth.api.LoginRequest;
import ir.tahamohamadi.auth.audit.AuthAuditService;
import ir.tahamohamadi.identity.assignment.UserRoleRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthAuditService authAuditService;
    private final TransactionTemplate transactionTemplate;

    public AuthenticationAttemptResult authenticate(LoginRequest request) {
        Optional<UUID> knownUserId = appUserRepository.findByNormalizedEmail(request.email()).map(AppUser::getId);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password())
            );
            AuthenticatedUserResponse authenticatedUser = transactionTemplate.execute(status -> {
                AppUser user = appUserRepository.findByNormalizedEmail(authentication.getName()).orElseThrow();
                user.recordSuccessfulAuthentication(Instant.now());
                authAuditService.recordSuccessfulLogin(user);
                return toResponse(user);
            });
            return AuthenticationAttemptResult.success(authentication, authenticatedUser);
        } catch (AuthenticationException exception) {
            transactionTemplate.executeWithoutResult(status -> {
                AppUser user = knownUserId.flatMap(appUserRepository::findById).orElse(null);
                if (user != null) {
                    user.recordFailedAuthentication(Instant.now());
                }
                authAuditService.recordFailedLogin(user);
            });
            return AuthenticationAttemptResult.failure();
        }
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse currentAuthenticatedUser(String email) {
        AppUser user = appUserRepository.findByNormalizedEmail(email).orElseThrow();
        return toResponse(user);
    }

    public void recordLogout(String email) {
        transactionTemplate.executeWithoutResult(status ->
                appUserRepository.findByNormalizedEmail(email).ifPresent(authAuditService::recordLogout)
        );
    }

    private AuthenticatedUserResponse toResponse(AppUser user) {
        return new AuthenticatedUserResponse(
                user.getId(),
                user.getDisplayName(),
                userRoleRepository.findActiveRoleCodesByUserId(user.getId())
        );
    }
}
