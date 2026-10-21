package com.raiz.domain.model;

/**
 * How far the pain reaches. Radiation below the knee (or the elbow) is one of the findings that best
 * separates radicular pain from referred pain.
 */
public enum PainDistribution {
    AXIAL_ONLY("Solo en la espalda o el cuello"),
    PROXIMAL("Se extiende a nalga o muslo (u hombro y brazo), sin pasar de la rodilla o el codo"),
    DISTAL("Baja por debajo de la rodilla (o del codo)");

    private final String label;

    PainDistribution(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
