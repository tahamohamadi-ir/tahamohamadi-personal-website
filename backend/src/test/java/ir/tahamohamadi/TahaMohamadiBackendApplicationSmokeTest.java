package ir.tahamohamadi;

import ir.tahamohamadi.identity.assignment.UserRoleRepository;
import ir.tahamohamadi.identity.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class TahaMohamadiBackendApplicationSmokeTest {

    @MockitoBean
    private AppUserRepository appUserRepository;

    @MockitoBean
    private UserRoleRepository userRoleRepository;

    @Test
    void contextLoads() {
    }
}
