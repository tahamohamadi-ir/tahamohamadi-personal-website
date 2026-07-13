package ir.tahamohamadi.content.page;
import ir.tahamohamadi.common.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface ContentPageTranslationRepository extends JpaRepository<ContentPageTranslation, UUID> {
 @Query("select t from ContentPageTranslation t join t.contentPage p where t.languageCode=:language and lower(t.slug)=lower(:slug) and t.deletedAt is null and p.deletedAt is null and p.status='PUBLISHED'") Optional<ContentPageTranslation> findPublishedByLanguageAndSlug(@Param("language") LanguageCode language,@Param("slug") String slug);
 Optional<ContentPageTranslation> findByContentPageIdAndLanguageCodeAndDeletedAtIsNull(UUID contentPageId, LanguageCode languageCode);
 List<ContentPageTranslation> findByContentPageIdInAndDeletedAtIsNull(Collection<UUID> contentPageIds);
}
