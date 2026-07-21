package ir.tahamohamadi.auth.qa;

import ir.tahamohamadi.identity.assignment.UserRole;
import ir.tahamohamadi.identity.assignment.UserRoleRepository;
import ir.tahamohamadi.identity.role.Role;
import ir.tahamohamadi.identity.role.RoleRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Profile("qa")
@RequiredArgsConstructor
public class QaAdminBootstrap implements ApplicationRunner {

    private static final String ADMIN_ROLE = "ADMIN";

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${TAHA_QA_ADMIN_EMAIL:}")
    private String email;

    @Value("${TAHA_QA_ADMIN_PASSWORD:}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String configuredEmail = requireConfiguredValue(email, "TAHA_QA_ADMIN_EMAIL");
        String configuredPassword = requireConfiguredValue(password, "TAHA_QA_ADMIN_PASSWORD");
        Instant now = Instant.now();

        AppUser user = appUserRepository.findByNormalizedEmail(configuredEmail)
                .orElseGet(() -> appUserRepository.save(AppUser.create(
                        configuredEmail,
                        passwordEncoder.encode(configuredPassword),
                        "QA Admin",
                        now
                )));

        Role adminRole = roleRepository.findActiveByCode(ADMIN_ROLE)
                .orElseThrow(() -> new IllegalStateException("The ADMIN role must exist before QA bootstrap."));

        if (!userRoleRepository.findActiveRoleCodesByUserId(user.getId()).contains(ADMIN_ROLE)) {
            userRoleRepository.save(UserRole.assign(user, adminRole, null, now));
        }
    }

    private static String requireConfiguredValue(String value, String variableName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(variableName + " is required when the qa profile is active.");
        }
        return value.trim();
    }
}
