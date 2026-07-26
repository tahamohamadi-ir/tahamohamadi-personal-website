package ir.tahamohamadi.portfolio.project;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface PortfolioProjectMediaRepository extends JpaRepository<PortfolioProjectMedia, PortfolioProjectMediaId> {
    @Query("select value from PortfolioProjectMedia value join fetch value.mediaAsset where value.id.portfolioProjectId=:projectId order by value.sortOrder asc")
    List<PortfolioProjectMedia> findByProjectIdWithAssetOrderBySortOrder(@Param("projectId") UUID projectId);
    @Modifying @Query("delete from PortfolioProjectMedia value where value.id.portfolioProjectId=:projectId")
    void deleteAllByProjectId(@Param("projectId") UUID projectId);
}
