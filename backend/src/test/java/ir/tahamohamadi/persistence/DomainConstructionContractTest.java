package ir.tahamohamadi.persistence;

import ir.tahamohamadi.content.featured.FeaturedItem;
import ir.tahamohamadi.portfolio.project.PortfolioProject;
import ir.tahamohamadi.publication.Publication;
import ir.tahamohamadi.resume.ResumeEntry;
import ir.tahamohamadi.skill.Skill;
import ir.tahamohamadi.skill.SkillCategory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class DomainConstructionContractTest {

    @Test
    void mutableWaveBAggregatesExposeControlledStaticFactories() {
        assertFactory(SkillCategory.class);
        assertFactory(Skill.class);
        assertFactory(PortfolioProject.class);
        assertFactory(Publication.class);
        assertFactory(ResumeEntry.class);
        assertFactory(FeaturedItem.class);
    }

    private void assertFactory(Class<?> type) {
        assertThat(Arrays.stream(type.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("create") && Modifier.isStatic(method.getModifiers())))
                .as("%s must provide a static create factory", type.getSimpleName())
                .isTrue();
    }
}
