package com.raiz.persistence;

import org.springframework.stereotype.Component;

import java.util.List;

/** Igual que el catálogo de raíces: se carga una vez al arrancar y queda inmutable en memoria. */
@Component
public class StrengthTestCatalog {

    private final List<StrengthTest> tests;

    StrengthTestCatalog(StrengthTestRepository repository) {
        // Sin colecciones perezosas: no hace falta abrir una transacción para mapear.
        this.tests = repository.findAllByOrderBySortOrderAsc().stream().map(StrengthTestEntity::toRecord).toList();
    }

    public List<StrengthTest> findAll() {
        return tests;
    }
}
