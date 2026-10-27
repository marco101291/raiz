package com.raiz.domain.assessment;

import com.raiz.domain.model.Aggravator;
import com.raiz.domain.model.PainDistribution;
import com.raiz.domain.model.PainQuality;
import com.raiz.domain.model.Reliever;
import com.raiz.domain.model.SpinalRegion;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Pain features reported in the questionnaire.
 *
 * @param intensity    numeric rating scale 0–10 (NRS)
 * @param durationDays days since the current episode started
 * @param reach        where the pain starts (neck, low back or both) and how far it reaches from each
 */
public record PainProfile(
        int intensity,
        int durationDays,
        Map<SpinalRegion, PainDistribution> reach,
        Set<PainQuality> qualities,
        Set<Aggravator> aggravators,
        Set<Reliever> relievers) {

    public PainProfile {
        if (intensity < 0 || intensity > 10) {
            throw new IllegalArgumentException("La intensidad debe estar entre 0 y 10");
        }
        if (durationDays < 0) {
            throw new IllegalArgumentException("La duración no puede ser negativa");
        }
        if (reach == null || reach.isEmpty()) {
            throw new IllegalArgumentException("Indica dónde empieza el dolor");
        }
        // EnumMap keeps the regions in declaration order, so derived texts are stable.
        reach = Collections.unmodifiableMap(new EnumMap<>(reach));
        qualities = ordered(qualities, PainQuality.class);
        aggravators = ordered(aggravators, Aggravator.class);
        relievers = ordered(relievers, Reliever.class);
    }

    /**
     * Immutable copy that iterates in the enum's declaration order. {@code Set.copyOf} has no defined order and
     * the JVM varies it between runs, so the pattern features would come out in a different order each time.
     */
    private static <E extends Enum<E>> Set<E> ordered(Set<E> values, Class<E> type) {
        EnumSet<E> copy = EnumSet.noneOf(type);
        if (values != null) {
            copy.addAll(values);
        }
        return Collections.unmodifiableSet(copy);
    }

    public Chronicity chronicity() {
        return Chronicity.fromDays(durationDays);
    }

    /** The pain goes past the elbow or the knee in at least one region. */
    public boolean reachesBeyondElbowOrKnee() {
        return reach.containsValue(PainDistribution.DISTAL);
    }

    public boolean hasNeuropathicQuality() {
        return qualities.stream().anyMatch(PainQuality::neuropathic);
    }

    public enum Chronicity {
        ACUTE("Empezó hace menos de 6 semanas"),
        SUBACUTE("Lleva de 6 a 12 semanas"),
        CHRONIC("Lleva más de 12 semanas");

        private final String label;

        Chronicity(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        static Chronicity fromDays(int days) {
            if (days < 42) return ACUTE;
            if (days <= 84) return SUBACUTE;
            return CHRONIC;
        }
    }
}
