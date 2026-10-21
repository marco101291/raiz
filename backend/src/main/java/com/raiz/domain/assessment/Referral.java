package com.raiz.domain.assessment;

import java.util.List;
import java.util.Objects;

/**
 * Referral to a specialist. Every assessment carries one: it is a domain invariant, not a web-layer
 * decision. That is why the constructor rejects null or empty values.
 *
 * @param reasons why it refers this way, in the order they are explained to the person
 */
public record Referral(Urgency urgency, List<Specialist> specialists, List<ReferralReason> reasons) {

    public Referral {
        Objects.requireNonNull(urgency, "urgency");
        if (specialists == null || specialists.isEmpty()) {
            throw new IllegalArgumentException("A referral must name at least one specialist");
        }
        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException("A referral must give at least one reason");
        }
        specialists = List.copyOf(specialists);
        reasons = List.copyOf(reasons);
    }
}
