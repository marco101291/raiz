package com.raiz.domain.model;

import java.util.Objects;

/**
 * Un hallazgo con su lado: una zona, un movimiento o un reflejo.
 * Ejemplo: {@code new Sided<>(BodyZone.GREAT_TOE, Side.RIGHT)}.
 */
public record Sided<T>(T finding, Side side) {

    public Sided {
        Objects.requireNonNull(finding, "finding");
        Objects.requireNonNull(side, "side");
    }

    public static <T> Sided<T> left(T finding) {
        return new Sided<>(finding, Side.LEFT);
    }

    public static <T> Sided<T> right(T finding) {
        return new Sided<>(finding, Side.RIGHT);
    }
}
