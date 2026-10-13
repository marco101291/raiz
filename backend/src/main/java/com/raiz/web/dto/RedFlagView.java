package com.raiz.web.dto;

import com.raiz.domain.model.RedFlag;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** @param emergency true = urgencias hoy; false = valoración médica pronto */
public record RedFlagView(String id, String label, boolean emergency) {

    public static RedFlagView of(RedFlag flag) {
        return new RedFlagView(flag.name(), flag.label(), flag.emergency());
    }

    public static List<RedFlagView> sorted(Collection<RedFlag> flags) {
        return flags.stream().sorted(Comparator.naturalOrder()).map(RedFlagView::of).toList();
    }
}
