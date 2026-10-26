package com.raiz.persistence;

import com.raiz.domain.model.MotorAction;

/** Everyday-task question for the neck, instead of a strength test. Form content, like {@link StrengthTest}. */
public record FunctionalQuestion(String id, String question, MotorAction motorAction) {
}
