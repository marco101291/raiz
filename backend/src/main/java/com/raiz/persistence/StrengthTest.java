package com.raiz.persistence;

import com.raiz.domain.model.MotorAction;

/** Guided strength test. It is form content, not engine logic: that is why it does not live in the domain. */
public record StrengthTest(String id, String name, String instructions, String safetyNote, MotorAction motorAction) {
}
