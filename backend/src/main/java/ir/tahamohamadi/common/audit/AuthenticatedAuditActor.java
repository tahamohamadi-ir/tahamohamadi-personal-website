package ir.tahamohamadi.common.audit;

import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedAuditActor {
    private final AppUserRepository users;
    public AuthenticatedAuditActor(AppUserRepository users) { this.users = users; }
    public AppUser required() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) throw new IllegalStateException("Authenticated actor is required");
        return users.findByNormalizedEmail(authentication.getName()).orElseThrow(() -> new IllegalStateException("Authenticated actor is unavailable"));
    }
}
