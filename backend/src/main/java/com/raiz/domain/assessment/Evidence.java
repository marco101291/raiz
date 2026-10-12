package com.raiz.domain.assessment;

import com.raiz.domain.model.BodyZone;
import com.raiz.domain.model.MotorAction;
import com.raiz.domain.model.Reflex;

/**
 * Por qué una raíz obtuvo puntos. Sealed para que el compilador obligue a manejar cada
 * tipo de hallazgo en los switch (peso, explicación, serialización). El lado lo lleva
 * {@link RootMatch}: toda la evidencia de un match es del mismo lado.
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
