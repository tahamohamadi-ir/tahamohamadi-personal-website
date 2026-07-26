package ir.tahamohamadi.content.page;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContentPageRevisionRepository extends JpaRepository<ContentPageRevision, UUID> {
    List<ContentPageRevision> findByContentPageIdOrderByRevisionNumberDesc(UUID pageId);
    Optional<ContentPageRevision> findFirstByContentPageIdOrderByRevisionNumberDesc(UUID pageId);
    Optional<ContentPageRevision> findByIdAndContentPageId(UUID id, UUID pageId);
}
