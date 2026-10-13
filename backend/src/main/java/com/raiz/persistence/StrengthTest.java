package com.raiz.persistence;

import com.raiz.domain.model.MotorAction;

/** Prueba de fuerza guiada. Es contenido de formulario, no del motor: por eso no vive en el dominio. */
public record StrengthTest(String id, String name, String instructions, String safetyNote, MotorAction motorAction) {
}
