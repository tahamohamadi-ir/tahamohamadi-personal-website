package ir.tahamohamadi.content.featured;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;
public interface FeaturedItemRepository extends JpaRepository<FeaturedItem,UUID>{
    @Query(value="select f from FeaturedItem f where f.slotKey=:slot and f.active=true and f.deletedAt is null and (f.startsAt is null or f.startsAt<=:at) and (f.endsAt is null or f.endsAt>=:at) order by f.sortOrder,f.id", countQuery="select count(f) from FeaturedItem f where f.slotKey=:slot and f.active=true and f.deletedAt is null and (f.startsAt is null or f.startsAt<=:at) and (f.endsAt is null or f.endsAt>=:at)") Page<FeaturedItem> findVisibleBySlot(@Param("slot") String slot,@Param("at") Instant at, Pageable pageable);
    Page<FeaturedItem> findByDeletedAtIsNull(Pageable pageable);
}
