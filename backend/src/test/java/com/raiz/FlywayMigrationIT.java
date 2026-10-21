package com.raiz;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** The migrations run on real PostgreSQL and leave the catalog described in the README. */
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

    @Test
    void everyRootHasSummaryNotesAndSource() {
        var incomplete = column("SELECT code FROM nerve_root WHERE patient_summary IS NULL "
                + "OR clinical_notes IS NULL OR source_reference IS NULL");

        assertThat(incomplete).isEmpty();
    }

    @Test
    void groinRootsWarnThatGroinPainMayComeFromLowerLevels() {
        var summaries = column("SELECT patient_summary FROM nerve_root WHERE code IN ('L1', 'L2')");

        assertThat(summaries).hasSize(2).allSatisfy(summary -> assertThat(summary)
                .contains("discos más bajos de la columna")
                .contains("pubalgia"));
    }

    @Test
    void everyOtherCauseHasZonesAndSource() {
        var incomplete = column("SELECT id FROM other_cause c WHERE c.source_reference = '' "
                + "OR NOT EXISTS (SELECT 1 FROM other_cause_zone z WHERE z.other_cause_id = c.id)");

        assertThat(column("SELECT id FROM other_cause")).isNotEmpty();
        assertThat(incomplete).isEmpty();
    }

    @Test
    void commonBilateralCausesCoverGroinAndMedianFingers() {
        var zones = column("SELECT DISTINCT z.zone FROM other_cause c JOIN other_cause_zone z ON z.other_cause_id = c.id "
                + "WHERE c.common_bilateral");

        assertThat(zones).containsExactlyInAnyOrder("GROIN", "THUMB", "INDEX_FINGER", "MIDDLE_FINGER");
    }

    private List<String> column(String sql) {
        return jdbc.sql(sql).query(String.class).list();
    }
}
