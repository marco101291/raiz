package com.raiz.domain.model;

/**
 * Zonas sensitivas del cuerpo. Los IDs coinciden con los del mapa SVG de dermatomas
 * en el frontend: el usuario toca una zona y el cliente envía este nombre.
 */
public enum BodyZone {
    // Miembro superior
    LATERAL_SHOULDER("Cara lateral del hombro y brazo"),
    LATERAL_FOREARM("Cara lateral del antebrazo"),
    THUMB("Pulgar"),
    INDEX_FINGER("Dedo índice"),
    MIDDLE_FINGER("Dedo medio"),
    RING_FINGER("Dedo anular"),
    LITTLE_FINGER("Dedo meñique"),
    MEDIAL_FOREARM("Cara medial del antebrazo"),
    MEDIAL_ARM("Cara medial del brazo"),

    // Miembro inferior
    GROIN("Ingle"),
    UPPER_ANTERIOR_THIGH("Parte alta y anterior del muslo"),
    ANTERIOR_THIGH("Cara anterior del muslo"),
    MEDIAL_KNEE("Cara medial de la rodilla"),
    MEDIAL_LEG("Cara medial de la pierna"),
    MEDIAL_MALLEOLUS("Tobillo, lado interno"),
    LATERAL_LEG("Cara lateral de la pierna"),
    DORSUM_FOOT("Empeine"),
    GREAT_TOE("Dedo gordo del pie"),
    LATERAL_FOOT("Borde externo del pie"),
    SOLE("Planta del pie"),
    POSTERIOR_CALF("Pantorrilla"),
    POSTERIOR_THIGH("Cara posterior del muslo");

    private final String label;

    BodyZone(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /**
     * Zonas donde los síntomas en ambos lados tienen causas frecuentes que no son urgentes, así que
     * por sí solos no indican compresión central:
     * <ul>
     *   <li>Ingle: el pubis está en la línea media y la pubalgia o las dos caderas duelen de ambos
     *       lados (Weir 2015, consenso de Doha).</li>
     *   <li>Pulgar, índice y medio: territorio del nervio mediano; el túnel carpiano es bilateral en
     *       la mayoría de los casos con estudio eléctrico (PLoS ONE 2021;16(12):e0260578).</li>
     * </ul>
     * Pendiente de revisión clínica (README, hoja de ruta, paso 5).
     */
    public boolean hasCommonBilateralCause() {
        return switch (this) {
            case GROIN, THUMB, INDEX_FINGER, MIDDLE_FINGER -> true;
            default -> false;
        };
    }

    public SpinalRegion region() {
        return switch (this) {
            case LATERAL_SHOULDER, LATERAL_FOREARM, THUMB, INDEX_FINGER, MIDDLE_FINGER, RING_FINGER,
                 LITTLE_FINGER, MEDIAL_FOREARM, MEDIAL_ARM -> SpinalRegion.CERVICAL;
            case GROIN, UPPER_ANTERIOR_THIGH, ANTERIOR_THIGH, MEDIAL_KNEE, MEDIAL_LEG, MEDIAL_MALLEOLUS,
                 LATERAL_LEG, DORSUM_FOOT, GREAT_TOE, LATERAL_FOOT, SOLE, POSTERIOR_CALF,
                 POSTERIOR_THIGH -> SpinalRegion.LUMBOSACRAL;
        };
    }
}
