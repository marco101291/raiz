package com.raiz.domain.assessment;

import com.raiz.domain.model.DiscLevel;
import com.raiz.domain.model.NerveRoot;
import com.raiz.domain.model.Side;

import java.util.List;
import java.util.Set;

/**
 * A root on a specific side ("L5 derecha") with the evidence that supports it.
 *
 * @param priorHerniationLevels levels of the person's prior herniation that typically compress this root;
 *                              shown to the professional, never added to the score
 */
public record RootMatch(NerveRoot root, Side side, List<Evidence> evidence, Set<DiscLevel> priorHerniationLevels) {

    public RootMatch {
        evidence = List.copyOf(evidence);
        priorHerniationLevels = Set.copyOf(priorHerniationLevels);
    }

    public RootMatch(NerveRoot root, Side side, List<Evidence> evidence) {
        this(root, side, evidence, Set.of());
    }

    public String label() {
        return root.code() + " " + side.rootLabel();
    }

    public int score() {
        return evidence.stream().mapToInt(Evidence::weight).sum();
    }

    /** There is a motor or reflex finding: suggests neurological compromise, not just irritation. */
    public boolean hasNeurologicalDeficit() {
        return evidence.stream().anyMatch(e -> switch (e) {
            case Evidence.MotorMatch m -> true;
            case Evidence.ReflexMatch r -> true;
            case Evidence.SensoryMatch s -> false;
        });
    }
}
