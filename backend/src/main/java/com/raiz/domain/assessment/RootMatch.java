package com.raiz.domain.assessment;

import com.raiz.domain.model.NerveRoot;
import com.raiz.domain.model.Side;

import java.util.List;

/** A root on a specific side ("L5 derecha") with the evidence that supports it. */
public record RootMatch(NerveRoot root, Side side, List<Evidence> evidence) {

    public RootMatch {
        evidence = List.copyOf(evidence);
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
