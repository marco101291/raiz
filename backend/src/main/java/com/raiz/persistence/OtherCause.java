package com.raiz.persistence;

import com.raiz.domain.model.BodyZone;

import java.util.Set;

/**
 * Otra causa posible de los síntomas en ciertas zonas (pubalgia, túnel carpiano…). Es contenido para
 * el resultado, no una regla del motor: por eso no vive en el dominio.
 */
public record OtherCause(String id, String title, String description, String sourceReference, Set<BodyZone> zones) {

    public OtherCause {
        zones = Set.copyOf(zones);
    }
}
