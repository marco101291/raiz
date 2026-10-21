package com.raiz.domain.model;

import java.util.Objects;

/**
 * A finding with its side: a zone, a movement or a reflex.
 * Example: {@code new Sided<>(BodyZone.GREAT_TOE, Side.RIGHT)}.
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
