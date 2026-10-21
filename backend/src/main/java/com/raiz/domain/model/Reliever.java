package com.raiz.domain.model;

/** What relieves the pain. */
public enum Reliever {
    LYING_DOWN("Acostarse"),
    WALKING("Caminar suave"),
    SITTING("Sentarse"),
    CHANGING_POSITION("Cambiar de postura con frecuencia"),
    NOTHING("Nada lo alivia");

    private final String label;

    Reliever(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
