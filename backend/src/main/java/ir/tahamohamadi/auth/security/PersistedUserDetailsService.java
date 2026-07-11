package ir.tahamohamadi.auth.security;

import ir.tahamohamadi.identity.assignment.UserRoleRepository;
import ir.tahamohamadi.identity.user.AppUser;
import ir.tahamohamadi.identity.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnBean(DataSource.class)
public class PersistedUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByNormalizedEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean accountNonLocked = appUser.getLockedUntil() == null
                || !appUser.getLockedUntil().isAfter(Instant.now());

        return User.withUsername(appUser.getEmail())
                .password(appUser.passwordHashForAuthentication())
                .authorities(userRoleRepository.findActiveRoleCodesByUserId(appUser.getId()).stream()
                        .map(roleCode -> new SimpleGrantedAuthority("ROLE_" + roleCode))
                        .toList())
                .disabled(!appUser.isEnabled())
                .accountLocked(!accountNonLocked)
                .build();
    }
}
