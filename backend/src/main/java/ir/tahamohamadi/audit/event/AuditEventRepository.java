package ir.tahamohamadi.audit.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    @Query("""
            select event
            from AuditEvent event
            where event.actor.id = :actorId
            order by event.occurredAt desc
            """)
    List<AuditEvent> findByActorIdOrderByOccurredAtDesc(@Param("actorId") UUID actorId);
}
