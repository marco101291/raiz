package com.raiz.persistence;

import com.raiz.domain.model.BodyZone;
import com.raiz.domain.model.MotorAction;
import com.raiz.domain.model.NerveRoot;
import com.raiz.domain.model.Reflex;
import com.raiz.domain.model.SpinalRegion;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fila de {@code nerve_root} con sus colecciones. Solo existe para leer el catálogo y convertirlo
 * al record del dominio; el contenido entra por migraciones Flyway, nunca desde la aplicación.
 */
@Entity
@Immutable
@Table(name = "nerve_root")
class NerveRootEntity {

    @Id
    private String code;

    @Enumerated(EnumType.STRING)
    private SpinalRegion region;

    private int sortOrder;

    @Column(columnDefinition = "text")
    private String patientSummary;

    @Column(columnDefinition = "text")
    private String clinicalNotes;

    @Column(columnDefinition = "text")
    private String sourceReference;

    @ElementCollection
    @CollectionTable(name = "nerve_root_dermatome", joinColumns = @JoinColumn(name = "root_code"))
    @Column(name = "zone")
    @Enumerated(EnumType.STRING)
    private Set<BodyZone> dermatome = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "nerve_root_myotome", joinColumns = @JoinColumn(name = "root_code"))
    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private Set<MotorAction> myotome = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "nerve_root_reflex", joinColumns = @JoinColumn(name = "root_code"))
    @Column(name = "reflex")
    @Enumerated(EnumType.STRING)
    private Set<Reflex> reflexes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "nerve_root_disc_level", joinColumns = @JoinColumn(name = "root_code"))
    @OrderColumn(name = "position")
    @Column(name = "level", columnDefinition = "text")
    private List<String> typicalDiscLevels = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "nerve_root_provocation_test", joinColumns = @JoinColumn(name = "root_code"))
    @OrderColumn(name = "position")
    @Column(name = "test_name", columnDefinition = "text")
    private List<String> provocationTests = new ArrayList<>();

    protected NerveRootEntity() {
        // Requerido por JPA.
    }

    /** Debe llamarse dentro de una transacción: las colecciones se cargan de forma perezosa. */
    NerveRoot toDomain() {
        return new NerveRoot(code, region, dermatome, myotome, reflexes, typicalDiscLevels, provocationTests,
                patientSummary, clinicalNotes, sourceReference);
    }
}
