package ir.tahamohamadi.content.social;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SocialLinkRepository extends JpaRepository<SocialLink,UUID>{
    List<SocialLink> findByActiveTrueAndDeletedAtIsNullOrderBySortOrderAscIdAsc();
    List<SocialLink> findTop50ByActiveTrueAndDeletedAtIsNullOrderBySortOrderAscIdAsc();
    Page<SocialLink> findByDeletedAtIsNull(Pageable pageable);
}
