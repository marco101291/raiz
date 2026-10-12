package com.raiz.domain.model;

public enum SpinalRegion {
    CERVICAL("Cervical"),
    LUMBOSACRAL("Lumbosacra");

    private final String label;

    SpinalRegion(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
