package com.raiz.domain.assessment;

import com.raiz.domain.model.RedFlag;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Resultado de una evaluación. Los tres casos están cerrados (sealed) y todos exponen
 * {@link #referral()}: no existe una evaluación sin derivación.
 */
public sealed interface Assessment {

    Referral referral();

    /** Lectura del patrón de dolor. NOT_ASSESSED si no hubo cuestionario o hubo alarma. */
    PatternInsight pattern();

    /**
     * Hubo datos de alarma: no se evalúa nada más, se deriva de inmediato.
     *
     * @param reported los que la persona marcó en el triage
     * @param inferred los que el motor detectó en los hallazgos (p. ej. síntomas en ambos lados)
     */
    record UrgentReferral(Set<RedFlag> reported, Set<RedFlag> inferred, Referral referral)
            implements Assessment {
        public UrgentReferral {
            reported = Set.copyOf(reported);
            inferred = Set.copyOf(inferred);
        }

        public Set<RedFlag> redFlags() {
            Set<RedFlag> all = EnumSet.noneOf(RedFlag.class);
            all.addAll(reported);
            all.addAll(inferred);
            return all;
        }

        @Override
        public PatternInsight pattern() {
            return PatternInsight.notAssessed();
        }
    }

    /** Raíces (con lado) ordenadas por compatibilidad con los síntomas reportados. */
    record RootCorrelation(PatternInsight pattern, List<RootMatch> matches, Referral referral)
            implements Assessment {
        public RootCorrelation {
            matches = List.copyOf(matches);
        }
    }

    /** No hay datos suficientes para correlacionar con una raíz; puede haber lectura de patrón. */
    record NoRootCorrelation(PatternInsight pattern, Note note, Referral referral) implements Assessment {

        /** Why there was no correlation. The text for the person lives outside the domain, as with {@link ReferralReason}. */
        public enum Note {
            /** Zones were marked, but none follows the territory of a root. */
            NO_TERRITORY_MATCH,
            /** Nerve-type pain with no zones marked: the map is missing. */
            RADICULAR_UNMAPPED,
            /** What was described is not nerve-type pain. */
            NOT_RADICULAR
        }
    }
}
