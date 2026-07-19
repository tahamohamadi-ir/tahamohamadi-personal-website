package ir.tahamohamadi.portfolio.project;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface PortfolioProjectSkillRepository extends JpaRepository<PortfolioProjectSkill, PortfolioProjectSkillId> {
    @Query("select value from PortfolioProjectSkill value join fetch value.skill where value.project.id=:projectId order by value.sortOrder asc, value.skill.id asc")
    List<PortfolioProjectSkill> findByProjectIdWithSkillOrderBySortOrder(@Param("projectId") UUID projectId);
    @Modifying @Query("delete from PortfolioProjectSkill value where value.project.id=:projectId")
    void deleteAllByProjectId(@Param("projectId") UUID projectId);
}
