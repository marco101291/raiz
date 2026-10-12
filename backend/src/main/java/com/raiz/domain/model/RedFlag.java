package com.raiz.domain.model;

/**
 * Datos de alarma. Cualquiera de ellos corta la evaluación educativa y deriva a urgencias
 * o a valoración médica prioritaria.
 */
public enum RedFlag {
    SADDLE_ANESTHESIA("Adormecimiento en zona genital, perineal o glúteos (\"en silla de montar\")", true),
    BLADDER_OR_BOWEL_DYSFUNCTION("Dificultad para orinar, retención o pérdida de control de esfínteres", true),
    PROGRESSIVE_WEAKNESS("Debilidad que aumenta con los días (p. ej. pie caído)", true),
    BILATERAL_SYMPTOMS("Síntomas en ambas piernas o ambos brazos", true),
    GAIT_OR_HAND_CLUMSINESS("Torpeza al caminar o al usar las manos (posible mielopatía)", true),
    FEVER("Fiebre asociada al dolor de espalda o cuello", false),
    UNEXPLAINED_WEIGHT_LOSS("Pérdida de peso sin explicación", false),
    CANCER_HISTORY("Antecedente de cáncer", false),
    SIGNIFICANT_TRAUMA("Golpe o caída fuerte reciente", false),
    UNRELENTING_NIGHT_PAIN("Dolor nocturno que no cede con ninguna postura", false);

    private final String label;
    private final boolean emergency;

    RedFlag(String label, boolean emergency) {
        this.label = label;
        this.emergency = emergency;
    }

    public String label() {
        return label;
    }

    /** true = urgencias hoy; false = valoración médica pronto, sin esperar. */
    public boolean emergency() {
        return emergency;
    }
}
