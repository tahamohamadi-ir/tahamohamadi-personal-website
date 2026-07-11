package ir.tahamohamadi.identity.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @Query("""
            select assignment
            from UserRole assignment
            where assignment.id.userId = :userId
            order by assignment.assignedAt asc
            """)
    List<UserRole> findByUserId(@Param("userId") UUID userId);

    @Query("""
            select role.code
            from UserRole assignment
            join assignment.role role
            where assignment.id.userId = :userId
              and role.active = true
              and role.deletedAt is null
            order by role.code asc
            """)
    List<String> findActiveRoleCodesByUserId(@Param("userId") UUID userId);
}
