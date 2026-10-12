package com.raiz;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Las migraciones corren en PostgreSQL real y dejan el catálogo del README. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class FlywayMigrationIT {

    @Autowired
    JdbcClient jdbc;

    @Test
    void seedsAllRootsInAnatomicalOrder() {
        var codes = jdbc.sql("SELECT code FROM nerve_root ORDER BY sort_order").query(String.class).list();

        assertThat(codes).containsExactly("C5", "C6", "C7", "C8", "T1", "L1", "L2", "L3", "L4", "L5", "S1");
    }

    @Test
    void l5TerritoryMatchesReadme() {
        assertThat(column("SELECT zone FROM nerve_root_dermatome WHERE root_code = 'L5'"))
                .containsExactlyInAnyOrder("LATERAL_LEG", "DORSUM_FOOT", "GREAT_TOE");
        assertThat(column("SELECT action FROM nerve_root_myotome WHERE root_code = 'L5'"))
                .containsExactlyInAnyOrder("GREAT_TOE_EXTENSION", "ANKLE_DORSIFLEXION", "HIP_ABDUCTION");
        assertThat(column("SELECT reflex FROM nerve_root_reflex WHERE root_code = 'L5'")).isEmpty();
        assertThat(column("SELECT test_name FROM nerve_root_provocation_test WHERE root_code = 'L5' ORDER BY position"))
                .containsExactly("SLR", "slump", "SLR cruzado");
    }

    @Test
    void everyRootHasDermatomeMyotomeAndDiscLevel() {
        for (String table : new String[] {"nerve_root_dermatome", "nerve_root_myotome", "nerve_root_disc_level"}) {
            var withoutRows = column("SELECT code FROM nerve_root r WHERE NOT EXISTS "
                    + "(SELECT 1 FROM " + table + " t WHERE t.root_code = r.code)");
            assertThat(withoutRows).as(table).isEmpty();
        }
    }

    private List<String> column(String sql) {
        return jdbc.sql(sql).query(String.class).list();
    }
}
