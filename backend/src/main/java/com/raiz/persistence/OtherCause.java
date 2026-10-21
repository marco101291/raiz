package com.raiz.persistence;

import com.raiz.domain.model.BodyZone;

import java.util.Set;

/**
 * Otra causa posible de los síntomas en ciertas zonas (pubalgia, túnel carpiano…). Es contenido para
 * el resultado, no una regla del motor: por eso no vive en el dominio.
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
