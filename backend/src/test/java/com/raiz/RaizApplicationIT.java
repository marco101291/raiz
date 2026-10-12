package com.raiz;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/** El contexto completo arranca contra PostgreSQL: dependencias, configuración y Flyway. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class RaizApplicationIT {

    @Test
    void contextLoads() {
    }
}
