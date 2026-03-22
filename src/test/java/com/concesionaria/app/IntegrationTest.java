package com.concesionaria.app;

import com.concesionaria.app.config.AsyncSyncConfiguration;
import com.concesionaria.app.config.DatabaseTestcontainer;
import com.concesionaria.app.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        ConcesionariaApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        com.concesionaria.app.config.JacksonHibernateConfiguration.class,
    }
)
@ImportTestcontainers(DatabaseTestcontainer.class)
public @interface IntegrationTest {}
