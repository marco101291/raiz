package com.raiz.domain.assessment;

import com.raiz.domain.model.NerveRoot;
import com.raiz.domain.model.Side;

import java.util.List;

/** Una raíz de un lado concreto ("L5 derecha") con la evidencia que la apoya. */
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

    /** Hay hallazgo motor o reflejo: sugiere compromiso neurológico, no solo irritación. */
    public boolean hasNeurologicalDeficit() {
        return evidence.stream().anyMatch(e -> switch (e) {
            case Evidence.MotorMatch m -> true;
            case Evidence.ReflexMatch r -> true;
            case Evidence.SensoryMatch s -> false;
        });
    }
}
