package com.raiz.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "result_text")
class ResultTextEntity {

    @Id
    private String code;

    @Column(columnDefinition = "text")
    private String text;

    protected ResultTextEntity() {
        // Required by JPA.
    }

    String code() {
        return code;
    }

    String text() {
        return text;
    }
}
