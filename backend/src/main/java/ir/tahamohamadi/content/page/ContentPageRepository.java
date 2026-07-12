package ir.tahamohamadi.content.page;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ContentPageRepository extends JpaRepository<ContentPage, UUID> { Optional<ContentPage> findByPageKeyAndDeletedAtIsNull(String pageKey); Page<ContentPage> findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc(Pageable pageable); }
