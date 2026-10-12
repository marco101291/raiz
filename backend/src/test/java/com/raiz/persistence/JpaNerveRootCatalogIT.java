package com.raiz.persistence;

import com.raiz.TestcontainersConfiguration;
import com.raiz.domain.assessment.Assessment;
import com.raiz.domain.assessment.CorrelationEngine;
import com.raiz.domain.assessment.SymptomReport;
import com.raiz.domain.model.BodyZone;
import com.raiz.domain.model.MotorAction;
import com.raiz.domain.model.NerveRoot;
import com.raiz.domain.model.Reflex;
import com.raiz.domain.model.Sided;
import com.raiz.domain.model.SpinalRegion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class JpaNerveRootCatalogIT {

    @Autowired
    JpaNerveRootCatalog catalog;

    @Autowired
    NerveRootRepository repository;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    CorrelationEngine engine;

    @Test
    void findAllFollowsAnatomicalOrderNotCodeOrder() {
        // Por código, T1 iría al final (C..., L..., S1, T1); en orden anatómico va antes de L1.
        assertThat(catalog.findAll()).extracting(NerveRoot::code)
                .containsExactly("C5", "C6", "C7", "C8", "T1", "L1", "L2", "L3", "L4", "L5", "S1");
    }

    @Test
    @Transactional // revierte el UPDATE al terminar
    void findAllOrdersBySortOrderColumn() {
        jdbc.sql("UPDATE nerve_root SET sort_order = 0 WHERE code = 'S1'").update();

        var reloaded = new JpaNerveRootCatalog(repository, transactionManager);

        assertThat(reloaded.findAll()).extracting(NerveRoot::code).startsWith("S1", "C5", "C6");
    }

    @Test
    void catalogIsImmutable() {
        assertThatThrownBy(() -> catalog.findAll().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void mapsEveryFieldIncludingOrderedLists() {
        assertThat(catalog.findByCode("L5")).get().satisfies(l5 -> {
            assertThat(l5.region()).isEqualTo(SpinalRegion.LUMBOSACRAL);
            assertThat(l5.dermatome()).containsExactlyInAnyOrder(
                    BodyZone.LATERAL_LEG, BodyZone.DORSUM_FOOT, BodyZone.GREAT_TOE);
            assertThat(l5.myotome()).containsExactlyInAnyOrder(
                    MotorAction.GREAT_TOE_EXTENSION, MotorAction.ANKLE_DORSIFLEXION, MotorAction.HIP_ABDUCTION);
            assertThat(l5.reflexes()).isEmpty();
            assertThat(l5.typicalDiscLevels()).containsExactly("L4-L5 posterolateral", "L5-S1 foraminal");
            assertThat(l5.provocationTests()).containsExactly("SLR", "slump", "SLR cruzado");
            assertThat(l5.patientSummary()).isNotBlank();
            assertThat(l5.clinicalNotes()).isNotBlank();
            assertThat(l5.sourceReference()).contains("Kreiner 2014");
        });
        assertThat(catalog.findByCode("C6")).get()
                .satisfies(c6 -> assertThat(c6.reflexes()).containsExactlyInAnyOrder(Reflex.BRACHIORADIALIS, Reflex.BICEPS));
    }

    @Test
    void unknownCodeIsEmpty() {
        assertThat(catalog.findByCode("X9")).isEmpty();
    }

    @Test
    void engineBeanUsesTheDatabaseCatalog() {
        var report = new SymptomReport(null, null,
                Set.of(Sided.right(BodyZone.GREAT_TOE), Sided.right(BodyZone.DORSUM_FOOT)), null, null);

        assertThat(engine.assess(report)).asInstanceOf(type(Assessment.RootCorrelation.class))
                .satisfies(c -> assertThat(c.matches().getFirst().label()).isEqualTo("L5 derecha"));
    }
}
