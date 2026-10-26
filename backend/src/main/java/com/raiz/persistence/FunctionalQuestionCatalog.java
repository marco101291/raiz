package com.raiz.persistence;

import org.springframework.stereotype.Component;

import java.util.List;

/** Like the strength test catalog: loaded once at startup and kept immutable in memory. */
@Component
public class FunctionalQuestionCatalog {

    private final List<FunctionalQuestion> questions;

    FunctionalQuestionCatalog(FunctionalQuestionRepository repository) {
        this.questions = repository.findAllByOrderBySortOrderAsc().stream()
                .map(FunctionalQuestionEntity::toRecord)
                .toList();
    }

    public List<FunctionalQuestion> findAll() {
        return questions;
    }
}
