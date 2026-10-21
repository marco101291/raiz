package com.raiz.persistence;

import com.raiz.domain.model.BodyZone;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.util.HashSet;
import java.util.Set;

@Entity
@Immutable
@Table(name = "other_cause")
class OtherCauseEntity {

    @Id
    private String id;

    private int sortOrder;

    @Column(columnDefinition = "text")
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String sourceReference;

    private boolean commonBilateral;

    @ElementCollection
    @CollectionTable(name = "other_cause_zone", joinColumns = @JoinColumn(name = "other_cause_id"))
    @Column(name = "zone")
    @Enumerated(EnumType.STRING)
    private Set<BodyZone> zones = new HashSet<>();

    protected OtherCauseEntity() {
        // Required by JPA.
    }

    OtherCause toRecord() {
        return new OtherCause(id, title, description, sourceReference, commonBilateral, zones);
    }
}
