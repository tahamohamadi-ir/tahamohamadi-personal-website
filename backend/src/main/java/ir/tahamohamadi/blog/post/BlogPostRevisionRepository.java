package ir.tahamohamadi.blog.post;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogPostRevisionRepository extends JpaRepository<BlogPostRevision, UUID> {
    List<BlogPostRevision> findByBlogPostIdOrderByRevisionNumberDesc(UUID postId);
    Optional<BlogPostRevision> findFirstByBlogPostIdOrderByRevisionNumberDesc(UUID postId);
    Optional<BlogPostRevision> findByIdAndBlogPostId(UUID id, UUID postId);
}
