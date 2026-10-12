package com.raiz.domain.model;

/**
 * Hasta dónde llega el dolor. La irradiación por debajo de la rodilla (o del codo) es uno de
 * los datos que más separa el dolor radicular del dolor referido.
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
