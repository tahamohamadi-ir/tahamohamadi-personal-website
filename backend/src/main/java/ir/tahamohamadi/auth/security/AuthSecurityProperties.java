package ir.tahamohamadi.auth.security;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "taha.security")
public class AuthSecurityProperties {

    @Min(4)
    @Max(31)
    private int bcryptStrength = 12;

    private boolean secureCookies = true;
}
