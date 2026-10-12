package io.github.hd4nielbu.lazystudio.bootstrap;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Un PostgreSQL real (misma versión que docker-compose.yml), compartido por las pruebas que arrancan la app. */
@TestConfiguration(proxyBeanMethods = false)
class PostgresDePrueba {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:18.6-alpine");
    }
}
