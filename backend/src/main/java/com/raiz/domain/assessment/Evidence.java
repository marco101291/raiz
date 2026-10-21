package com.raiz.domain.assessment;

import com.raiz.domain.model.BodyZone;
import com.raiz.domain.model.MotorAction;
import com.raiz.domain.model.Reflex;

/**
 * Why a root scored points. Sealed so the compiler forces every switch (weight, explanation,
 * serialization) to handle each kind of finding. The side lives in {@link RootMatch}: all the evidence
 * of a match is on the same side.
 */
public sealed interface Evidence {

    int weight();

    String explanation();

    record SensoryMatch(BodyZone zone) implements Evidence {
        public int weight() {
            return 2;
        }

        public String explanation() {
            return "Síntomas sensitivos en: " + zone.label().toLowerCase();
        }
    }

    record MotorMatch(MotorAction action) implements Evidence {
        public int weight() {
            return 3;
        }

        public String explanation() {
            return "Debilidad al: " + action.label().toLowerCase();
        }
    }

    record ReflexMatch(Reflex reflex) implements Evidence {
        public int weight() {
            return 3;
        }

        public String explanation() {
            return "Reflejo " + reflex.label().toLowerCase() + " disminuido";
        }
    }
}
