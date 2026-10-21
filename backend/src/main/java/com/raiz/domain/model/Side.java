package com.raiz.domain.model;

public enum Side {
    LEFT("lado izquierdo", "izquierda"),
    RIGHT("lado derecho", "derecha");

    private final String label;
    private final String rootLabel;

    Side(String label, String rootLabel) {
        this.label = label;
        this.rootLabel = rootLabel;
    }

    /** "lado derecho", for zones and movements. */
    public String label() {
        return label;
    }

    /** "derecha", for naming the root: "L5 derecha". */
    public String rootLabel() {
        return rootLabel;
    }
}
