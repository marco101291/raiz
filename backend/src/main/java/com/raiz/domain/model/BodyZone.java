package com.raiz.domain.model;

/**
 * Sensory zones of the body. The IDs match those of the SVG dermatome map in the frontend: the user
 * taps a zone and the client sends this name.
 */
public enum BodyZone {
    // Upper limb
    LATERAL_SHOULDER("Cara lateral del hombro y brazo"),
    LATERAL_FOREARM("Cara lateral del antebrazo"),
    THUMB("Pulgar"),
    INDEX_FINGER("Dedo índice"),
    MIDDLE_FINGER("Dedo medio"),
    RING_FINGER("Dedo anular"),
    LITTLE_FINGER("Dedo meñique"),
    MEDIAL_FOREARM("Cara medial del antebrazo"),
    MEDIAL_ARM("Cara medial del brazo"),

    // Lower limb
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
