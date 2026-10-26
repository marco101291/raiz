package com.raiz.domain.model;

/**
 * Red flags. Any of them stops the educational assessment and refers to the emergency room or to a
 * prompt medical visit.
 */
public enum RedFlag {
    SADDLE_ANESTHESIA(
            "Se te duerme o no sientes la zona entre las piernas: genitales, ano o la parte interna de los glúteos",
            true),
    BLADDER_OR_BOWEL_DYSFUNCTION(
            "Te cuesta orinar o no puedes, o se te escapa la orina o el excremento sin querer",
            true),
    PROGRESSIVE_WEAKNESS(
            "Pierdes fuerza en un brazo o una pierna y va a más con los días; por ejemplo, el pie se te arrastra",
            true),
    BILATERAL_SYMPTOMS(
            "Sientes hormigueo, adormecimiento o debilidad en las dos piernas o en los dos brazos",
            true),
    GAIT_OR_HAND_CLUMSINESS(
            "Notas torpeza nueva al caminar o con las manos: se te caen las cosas o te cuesta abrocharte",
            true),
    FEVER("Tienes fiebre además del dolor de espalda o de cuello", false),
    UNEXPLAINED_WEIGHT_LOSS("Has bajado de peso sin proponértelo", false),
    CANCER_HISTORY("Has tenido cáncer alguna vez", false),
    SIGNIFICANT_TRAUMA("El dolor empezó después de un golpe o una caída fuerte", false),
    UNRELENTING_NIGHT_PAIN("De noche el dolor no te deja descansar en ninguna postura", false);

    private final String label;
    private final boolean emergency;

    RedFlag(String label, boolean emergency) {
        this.label = label;
        this.emergency = emergency;
    }

    public String label() {
        return label;
    }

    /** true = emergency room today; false = prompt medical visit, without waiting. */
    public boolean emergency() {
        return emergency;
    }
}
