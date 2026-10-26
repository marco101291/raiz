package com.raiz.domain.model;

/**
 * How far the pain reaches from where it starts. Radiation below the knee (or the elbow) is one of the
 * findings that best separates radicular pain from referred pain. The wording depends on the region, so a
 * person with neck pain never has to translate "knee" into "elbow".
 */
public enum PainDistribution {
    AXIAL_ONLY("Solo en el cuello", "Solo en la espalda baja"),
    PROXIMAL("Baja al hombro o al brazo, sin pasar del codo", "Baja a la nalga o al muslo, sin pasar de la rodilla"),
    DISTAL("Pasa del codo y puede llegar a la mano", "Pasa de la rodilla y puede llegar al pie");

    private final String cervicalLabel;
    private final String lumbosacralLabel;

    PainDistribution(String cervicalLabel, String lumbosacralLabel) {
        this.cervicalLabel = cervicalLabel;
        this.lumbosacralLabel = lumbosacralLabel;
    }

    public String label(SpinalRegion region) {
        return switch (region) {
            case CERVICAL -> cervicalLabel;
            case LUMBOSACRAL -> lumbosacralLabel;
        };
    }
}
