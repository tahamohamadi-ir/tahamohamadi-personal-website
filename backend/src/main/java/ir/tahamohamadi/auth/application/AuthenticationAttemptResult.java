package ir.tahamohamadi.auth.application;

import ir.tahamohamadi.auth.api.AuthenticatedUserResponse;
import org.springframework.security.core.Authentication;

public record AuthenticationAttemptResult(Authentication authentication, AuthenticatedUserResponse authenticatedUser) {

    public static AuthenticationAttemptResult success(
            Authentication authentication,
            AuthenticatedUserResponse authenticatedUser
    ) {
        return new AuthenticationAttemptResult(authentication, authenticatedUser);
    }

    public static AuthenticationAttemptResult failure() {
        return new AuthenticationAttemptResult(null, null);
    }

    public boolean isSuccessful() {
        return authentication != null;
    }
}
