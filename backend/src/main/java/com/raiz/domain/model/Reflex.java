package com.raiz.domain.model;

/** Deep tendon reflexes. Only a professional can examine them reliably. */
public enum Reflex {
    BICEPS("Bicipital"),
    BRACHIORADIALIS("Braquiorradial (estilorradial)"),
    TRICEPS("Tricipital"),
    PATELLAR("Rotuliano"),
    ACHILLES("Aquíleo");

    private final String label;

    Reflex(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public SpinalRegion region() {
        return switch (this) {
            case BICEPS, BRACHIORADIALIS, TRICEPS -> SpinalRegion.CERVICAL;
            case PATELLAR, ACHILLES -> SpinalRegion.LUMBOSACRAL;
        };
    }
}
