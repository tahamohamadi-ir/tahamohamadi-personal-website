package ir.tahamohamadi.identity.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    @Query("""
            select u
            from AppUser u
            where u.deletedAt is null
              and lower(u.email) = lower(trim(:email))
            """)
    Optional<AppUser> findByNormalizedEmail(@Param("email") String email);

    @Query("""
            select count(u) > 0
            from AppUser u
            where u.deletedAt is null
              and lower(u.email) = lower(trim(:email))
            """)
    boolean existsByNormalizedEmail(@Param("email") String email);
}
