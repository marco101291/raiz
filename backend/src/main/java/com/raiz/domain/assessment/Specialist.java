package com.raiz.domain.assessment;

public enum Specialist {
    EMERGENCY_ROOM("Servicio de urgencias"),
    PHYSICIAN("Médico (general o especialista en columna)"),
    SPINE_SURGEON("Traumatólogo de columna o neurocirujano"),
    NEUROLOGIST("Neurólogo"),
    PHYSIATRIST("Médico especialista en medicina física y rehabilitación"),
    PHYSIOTHERAPIST("Fisioterapeuta");

    private final String label;

    Specialist(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
