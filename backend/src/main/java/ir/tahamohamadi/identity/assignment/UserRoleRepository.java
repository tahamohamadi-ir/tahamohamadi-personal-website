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
}
