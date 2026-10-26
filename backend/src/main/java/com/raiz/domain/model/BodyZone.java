package com.raiz.domain.model;

/**
 * Sensory zones of the body. The IDs match those of the SVG dermatome map in the frontend: the user
 * taps a zone and the client sends this name.
 */
public enum BodyZone {
    // Upper limb
    LATERAL_SHOULDER("Hombro y brazo, por fuera"),
    LATERAL_FOREARM("Antebrazo, lado del pulgar"),
    THUMB("Pulgar"),
    INDEX_FINGER("Dedo índice"),
    MIDDLE_FINGER("Dedo medio"),
    RING_FINGER("Dedo anular"),
    LITTLE_FINGER("Dedo meñique"),
    MEDIAL_FOREARM("Antebrazo, lado del meñique"),
    MEDIAL_ARM("Brazo, por dentro"),

    // Lower limb
    GROIN("Ingle"),
    UPPER_ANTERIOR_THIGH("Muslo por delante, parte de arriba"),
    ANTERIOR_THIGH("Muslo por delante, hasta la rodilla"),
    MEDIAL_KNEE("Rodilla, por dentro"),
    MEDIAL_LEG("Debajo de la rodilla, por dentro"),
    MEDIAL_MALLEOLUS("Tobillo, por dentro"),
    LATERAL_LEG("Debajo de la rodilla, por fuera"),
    DORSUM_FOOT("Empeine"),
    GREAT_TOE("Dedo gordo del pie"),
    LATERAL_FOOT("Pie, borde de afuera"),
    SOLE("Planta del pie"),
    POSTERIOR_CALF("Pantorrilla"),
    POSTERIOR_THIGH("Muslo por detrás");

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
