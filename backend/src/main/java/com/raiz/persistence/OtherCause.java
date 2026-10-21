package com.raiz.persistence;

import com.raiz.domain.model.BodyZone;

import java.util.Set;

/**
 * Another possible cause of the symptoms in certain zones (pubalgia, carpal tunnel…). It is content for the
 * result, not an engine rule: that is why it does not live in the domain.
 *
 * @param commonBilateral whether it usually causes symptoms on both sides; the engine uses it, through
 *                        {@link com.raiz.domain.port.CommonBilateralCauses}, so it is not treated as a red flag
 */
public record OtherCause(String id, String title, String description, String sourceReference,
                         boolean commonBilateral, Set<BodyZone> zones) {

    public OtherCause {
        zones = Set.copyOf(zones);
    }
}
