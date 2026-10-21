package com.raiz.domain.model;

/** What makes the pain worse. Several point to a disc origin (flexion, sitting, Valsalva). */
public enum Aggravator {
    SITTING("Estar sentado", true),
    FORWARD_BENDING("Agacharse o inclinarse hacia adelante", true),
    COUGH_SNEEZE("Toser, estornudar o pujar", true),
    LIFTING("Cargar peso", true),
    BACKWARD_BENDING("Inclinarse hacia atrás", false),
    STANDING_LONG("Estar de pie mucho tiempo", false),
    WALKING("Caminar", false),
    LYING_DOWN("Estar acostado", false);

    private final String label;
    private final boolean discogenicHint;

    Aggravator(String label, boolean discogenicHint) {
        this.label = label;
        this.discogenicHint = discogenicHint;
    }

    public String label() {
        return label;
    }

    public boolean discogenicHint() {
        return discogenicHint;
    }
}
