package com.raiz.persistence;

import org.springframework.stereotype.Component;

import java.util.List;

/** Like the root catalog: loaded once at startup and kept immutable in memory. */
@Component
public class StrengthTestCatalog {

    private final List<StrengthTest> tests;

    StrengthTestCatalog(StrengthTestRepository repository) {
        // No lazy collections: no transaction is needed to map.
        this.tests = repository.findAllByOrderBySortOrderAsc().stream().map(StrengthTestEntity::toRecord).toList();
    }

    public List<StrengthTest> findAll() {
        return tests;
    }
}
