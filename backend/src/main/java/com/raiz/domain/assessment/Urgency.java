package com.raiz.domain.assessment;

public enum Urgency {
    EMERGENCY_NOW("Acude a urgencias hoy"),
    PROMPT("Busca valoración médica en los próximos días"),
    ROUTINE("Agenda una valoración con un especialista");

    private final String label;

    Urgency(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
