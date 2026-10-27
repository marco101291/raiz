package com.raiz.domain.assessment;

import com.raiz.domain.model.DiscLevel;

import java.util.Objects;
import java.util.Set;

/**
 * Relevant history the person reports. It never changes scores or urgency: the engine only points out when a
 * compatible root matches the level of a prior herniation, for the professional to weigh.
 *
 * @param herniationLevels levels of the prior herniation, if known; only allowed when it was diagnosed
 */
public record MedicalHistory(
        PriorHerniation priorHerniation, Set<DiscLevel> herniationLevels, boolean priorSpineSurgery) {

    public static final MedicalHistory NONE = new MedicalHistory(PriorHerniation.UNKNOWN, Set.of(), false);

    public enum PriorHerniation { YES, NO, UNKNOWN }

    public MedicalHistory {
        Objects.requireNonNull(priorHerniation, "priorHerniation");
        herniationLevels = herniationLevels == null ? Set.of() : Set.copyOf(herniationLevels);
        if (priorHerniation != PriorHerniation.YES && !herniationLevels.isEmpty()) {
            throw new IllegalArgumentException("Los niveles de la hernia solo aplican si te la diagnosticaron");
        }
    }
}
