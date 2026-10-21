package com.raiz.domain.port;

import com.raiz.domain.model.BodyZone;

/**
 * Outbound port: which zones have common, non-urgent causes that produce symptoms on both sides
 * (pubalgia in the groin, carpal tunnel in the fingers). This is sourced clinical knowledge, so it lives
 * in the catalog rather than in code: adding a zone is an INSERT, not an engine change.
 */
@FunctionalInterface
public interface CommonBilateralCauses {

    boolean existFor(BodyZone zone);
}
