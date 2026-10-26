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
@Table(name = "functional_question")
class FunctionalQuestionEntity {

    @Id
    private String id;

    private int sortOrder;

    @Column(columnDefinition = "text")
    private String question;

    @Enumerated(EnumType.STRING)
    private MotorAction motorAction;

    protected FunctionalQuestionEntity() {
        // Required by JPA.
    }

    FunctionalQuestion toRecord() {
        return new FunctionalQuestion(id, question, motorAction);
    }
}
