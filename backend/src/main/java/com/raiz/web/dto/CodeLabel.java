package com.raiz.web.dto;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/** An enum value with its Spanish label, so the frontend does not duplicate texts. */
public record CodeLabel(String id, String label) {

    public static CodeLabel of(Enum<?> value, String label) {
        return new CodeLabel(value.name(), label);
    }

    /** Sorts by the enum's declaration order so the response is stable. */
    public static <E extends Enum<E>> List<CodeLabel> sorted(Collection<E> values, Function<E, String> label) {
        return values.stream()
                .sorted(Comparator.naturalOrder())
                .map(v -> of(v, label.apply(v)))
                .toList();
    }
}
