package com.raiz.domain.assessment;

import com.raiz.domain.model.SpinalRegion;

import java.util.Objects;

/**
 * Why the engine refers the way it does. The engine decides the reason; how it is explained to the
 * person is sourced content that lives outside the domain (table {@code result_text}).
 *
 * @param region only for {@link Kind#BILATERAL_COMMON_CAUSE}: the other causes and the warning signs
 *               differ between the groin (lumbosacral) and the hands (cervical)
 */
public record ReferralReason(Kind kind, SpinalRegion region) {

    public enum Kind {
        /** Red flag that requires going to the emergency room today. */
        EMERGENCY_RED_FLAG,
        /** Non-emergency red flag: causes other than a disc herniation must be ruled out. */
        RED_FLAG_RULE_OUT,
        NEUROLOGICAL_DEFICIT,
        SEVERE_PAIN,
        CHRONIC_RADICULAR,
        /** Sensory symptoms of a root, without loss of strength or reflexes. */
        SENSORY_ONLY,
        /** Zones were marked, but none matches the territory of a root in the catalog. */
        NO_TERRITORY_MATCH,
        /** Nerve-type pain running down the limb, with no zones marked on the map. */
        RADICULAR_UNMAPPED,
        CHRONIC_PAIN,
        /** Pain without red flags or nerve features: it is expected to improve on its own. */
        SELF_LIMITED,
        /** Symptoms on both sides, but only in zones with common, non-urgent causes. */
        BILATERAL_COMMON_CAUSE
    }

    public ReferralReason {
        Objects.requireNonNull(kind, "kind");
        if ((kind == Kind.BILATERAL_COMMON_CAUSE) != (region != null)) {
            throw new IllegalArgumentException("Only " + Kind.BILATERAL_COMMON_CAUSE + " carries a region");
        }
    }

    public static ReferralReason of(Kind kind) {
        return new ReferralReason(kind, null);
    }

    public static ReferralReason bilateralCommonCause(SpinalRegion region) {
        return new ReferralReason(Kind.BILATERAL_COMMON_CAUSE, Objects.requireNonNull(region, "region"));
    }
}
