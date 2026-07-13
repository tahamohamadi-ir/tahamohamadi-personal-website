package ir.tahamohamadi.portfolio.project;

import ir.tahamohamadi.skill.Skill;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "portfolio_project_skill")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioProjectSkill {
    @EmbeddedId private PortfolioProjectSkillId id;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("projectId") @JoinColumn(name = "portfolio_project_id") private PortfolioProject project;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("skillId") @JoinColumn(name = "skill_id") private Skill skill;
    @Column(name = "sort_order", nullable = false) private int sortOrder;

    private PortfolioProjectSkill(PortfolioProject project, Skill skill, int sortOrder) {
        if (sortOrder < 0) throw new IllegalArgumentException("sortOrder must be nonnegative");
        this.id = new PortfolioProjectSkillId(project.getId(), skill.getId());
        this.project = project;
        this.skill = skill;
        this.sortOrder = sortOrder;
    }

    public static PortfolioProjectSkill assign(PortfolioProject project, Skill skill, int sortOrder) {
        return new PortfolioProjectSkill(project, skill, sortOrder);
    }
}
