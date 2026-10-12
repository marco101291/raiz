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

    /** "lado derecho", para zonas y movimientos. */
    public String label() {
        return label;
    }

    /** "derecha", para nombrar la raíz: "L5 derecha". */
    public String rootLabel() {
        return rootLabel;
    }
}
