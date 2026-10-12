package com.raiz.domain.assessment;

import java.util.List;
import java.util.Objects;

/**
 * Derivación a un especialista. Toda evaluación la lleva: es una invariante del dominio,
 * no una decisión de la capa web. Por eso el constructor no acepta valores nulos ni vacíos.
 */
public record Referral(Urgency urgency, List<Specialist> specialists, String message) {

    public static final String DISCLAIMER =
            "Esta herramienta es educativa y no sustituye una valoración médica. "
            + "Solo un profesional puede confirmar qué estructura está afectada.";

    public Referral {
        Objects.requireNonNull(urgency, "urgency");
        Objects.requireNonNull(message, "message");
        if (specialists == null || specialists.isEmpty()) {
            throw new IllegalArgumentException("Una derivación debe nombrar al menos un especialista");
        }
        specialists = List.copyOf(specialists);
    }

    public String disclaimer() {
        return DISCLAIMER;
    }
}
