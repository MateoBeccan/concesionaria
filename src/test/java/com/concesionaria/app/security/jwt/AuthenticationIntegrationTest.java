package com.concesionaria.app.security.jwt;

import com.concesionaria.app.config.SecurityConfiguration;
import com.concesionaria.app.config.SecurityJwtConfiguration;
import com.concesionaria.app.config.WebConfigurer;
import com.concesionaria.app.management.SecurityMetersService;
import com.concesionaria.app.web.rest.AuthenticateController;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import tech.jhipster.config.JHipsterProperties;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    properties = {
        "jhipster.security.authentication.jwt.base64-secret=ZGV2ZWxvcG1lbnQtb25seS1oczUxMi1qd3Qtc2VjcmV0LWR1bW15LWNoYW5nZS1tZS02NC1ieXRlcy1taW5pbXVt",
        "jhipster.security.authentication.jwt.token-validity-in-seconds=60000",
    },
    classes = {
        JHipsterProperties.class,
        WebConfigurer.class,
        SecurityConfiguration.class,
        SecurityJwtConfiguration.class,
        SecurityMetersService.class,
        AuthenticateController.class,
        JwtAuthenticationTestUtils.class,
    }
)
public @interface AuthenticationIntegrationTest {}
