package ir.tahamohamadi.portfolio.project;

import ir.tahamohamadi.common.domain.LanguageCode;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface PortfolioProjectTranslationRepository extends JpaRepository<PortfolioProjectTranslation, UUID> {
    @Query("select t from PortfolioProjectTranslation t join t.project p where t.languageCode=:language and t.deletedAt is null and p.deletedAt is null and p.status='PUBLISHED' order by p.sortOrder,p.id")
    List<PortfolioProjectTranslation> findPublishedByLanguage(@Param("language") LanguageCode language);
    @Query("select t from PortfolioProjectTranslation t join t.project p where t.languageCode=:language and lower(t.slug)=lower(:slug) and t.deletedAt is null and p.deletedAt is null and p.status='PUBLISHED'")
    Optional<PortfolioProjectTranslation> findPublishedByLanguageAndSlug(@Param("language") LanguageCode language, @Param("slug") String slug);
    List<PortfolioProjectTranslation> findByProjectIdAndDeletedAtIsNull(UUID projectId);
    List<PortfolioProjectTranslation> findByProjectIdInAndDeletedAtIsNull(Collection<UUID> projectIds);
    List<PortfolioProjectTranslation> findByProjectIdInAndLanguageCodeAndDeletedAtIsNull(Collection<UUID> projectIds, LanguageCode language);
}
