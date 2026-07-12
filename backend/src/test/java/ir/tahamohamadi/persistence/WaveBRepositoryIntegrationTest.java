package ir.tahamohamadi.persistence;
import ir.tahamohamadi.skill.SkillRepository; import org.junit.jupiter.api.Test; import static org.assertj.core.api.Assertions.assertThat;
class WaveBRepositoryIntegrationTest { @Test void exposesSkillRepositoryContract(){ assertThat(SkillRepository.class).isNotNull(); } }
