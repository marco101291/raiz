package com.raiz.persistence;

import com.raiz.domain.model.MotorAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "strength_test")
class StrengthTestEntity {

    @Id
    private String id;

    private int sortOrder;

    @Column(columnDefinition = "text")
    private String name;

    @Column(columnDefinition = "text")
    private String instructions;

    @Column(columnDefinition = "text")
    private String safetyNote;

    @Enumerated(EnumType.STRING)
    private MotorAction motorAction;

    protected StrengthTestEntity() {
        // Required by JPA.
    }

    StrengthTest toRecord() {
        return new StrengthTest(id, name, instructions, safetyNote, motorAction);
    }
}
