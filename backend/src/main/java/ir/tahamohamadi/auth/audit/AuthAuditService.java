package ir.tahamohamadi.auth.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.identity.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthAuditService {

    private static final String TARGET_TYPE = "app_user";

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public void recordSuccessfulLogin(AppUser user) {
        record(user, "AUTH_LOGIN", "SUCCESS", "PASSWORD");
    }

    public void recordFailedLogin(AppUser user) {
        record(user, "AUTH_LOGIN_FAILED", "FAILURE", "PASSWORD");
    }

    public void recordLogout(AppUser user) {
        record(user, "AUTH_LOGOUT", "SUCCESS", "SESSION");
    }

    private void record(AppUser user, String action, String outcome, String authMethod) {
        auditEventRepository.save(AuditEvent.record(
                UUID.randomUUID(),
                Instant.now(),
                user,
                action,
                TARGET_TYPE,
                user == null ? null : user.getId(),
                outcome,
                null,
                null,
                objectMapper.createObjectNode().put("auth_method", authMethod)
        ));
    }
}
