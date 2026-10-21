package com.raiz.domain.assessment;

import java.util.List;

/**
 * Educational reading of the pain pattern ("is it a herniation?"). It describes compatibilities, never a
 * diagnosis.
 *
 * The explanation of each type is text for the person and lives outside the domain (table {@code result_text}).
 *
 * @param features questionnaire features that support the reading, in plain language
 */
public record PatternInsight(Type type, PainProfile.Chronicity chronicity, List<String> features) {

    public PatternInsight {
        features = List.copyOf(features);
    }

    public static PatternInsight notAssessed() {
        return new PatternInsight(Type.NOT_ASSESSED, null, List.of());
    }

    public enum Type {
        /** Nerve-type pain that follows a limb: compatible with root irritation. */
        RADICULAR,
        /** Worse with flexion, sitting or Valsalva: compatible with a disc origin without root irritation. */
        DISCOGENIC,
        /** Worse with extension or standing and better sitting: compatible with a facet joint origin. */
        EXTENSION_SENSITIVE,
        /** No features pointing to a specific structure. */
        NONSPECIFIC,
        NOT_ASSESSED
    }
}
