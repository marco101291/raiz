package com.raiz;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/** The full context starts against PostgreSQL: dependencies, configuration and Flyway. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class RaizApplicationIT {

    @Test
    void contextLoads() {
    }
}
