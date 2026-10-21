package com.raiz.domain.assessment;

import com.raiz.domain.model.RedFlag;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Result of an assessment. The three cases are closed (sealed) and all expose {@link #referral()}: there
 * is no assessment without a referral.
 */
public sealed interface Assessment {

    Referral referral();

    /** Reading of the pain pattern. NOT_ASSESSED if there was no questionnaire or there was a red flag. */
    PatternInsight pattern();

    /**
     * There were red flags: nothing else is assessed, the person is referred right away.
     *
     * @param reported the ones the person marked in the triage
     * @param inferred the ones the engine detected in the findings (e.g. symptoms on both sides)
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

    /** Roots (with side) ordered by compatibility with the reported symptoms. */
    record RootCorrelation(PatternInsight pattern, List<RootMatch> matches, Referral referral)
            implements Assessment {
        public RootCorrelation {
            matches = List.copyOf(matches);
        }
    }

    /** Not enough data to correlate with a root; there may be a pattern reading. */
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
