package com.raiz.web.dto;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/** Un valor de enum con su etiqueta en español, para que el frontend no duplique textos. */
public record CodeLabel(String id, String label) {

    public static CodeLabel of(Enum<?> value, String label) {
        return new CodeLabel(value.name(), label);
    }

    /** Ordena por el orden de declaración del enum para que la respuesta sea estable. */
    public static <E extends Enum<E>> List<CodeLabel> sorted(Collection<E> values, Function<E, String> label) {
        return values.stream()
                .sorted(Comparator.naturalOrder())
                .map(v -> of(v, label.apply(v)))
                .toList();
    }
}
