package ir.tahamohamadi.content.featured;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FeaturedItemRepository extends JpaRepository<FeaturedItem, UUID> {
    @Query("select f from FeaturedItem f left join fetch f.blogPost left join fetch f.portfolioProject left join fetch f.publication where f.slotKey=:slot and f.active=true and f.deletedAt is null and (f.startsAt is null or f.startsAt<=:at) and (f.endsAt is null or f.endsAt>=:at) order by f.sortOrder,f.id")
    List<FeaturedItem> findVisibleBySlot(@Param("slot") String slot, @Param("at") Instant at, Pageable pageable);

    org.springframework.data.domain.Page<FeaturedItem> findByDeletedAtIsNull(Pageable pageable);
}
