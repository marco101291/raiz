package com.raiz.domain.model;

/**
 * @param label       technical name, used to group zones and roots
 * @param originLabel plain-language name for "where does the pain start?"
 */
public enum SpinalRegion {
    CERVICAL("Cervical", "Cuello"),
    LUMBOSACRAL("Lumbosacra", "Espalda baja");

    private final String label;
    private final String originLabel;

    SpinalRegion(String label, String originLabel) {
        this.label = label;
        this.originLabel = originLabel;
    }

    public String label() {
        return label;
    }

    public String originLabel() {
        return originLabel;
    }
}
