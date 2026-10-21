package com.raiz.web.dto;

import java.util.List;

/** Responses of {@code /api/v1/catalog/**}. */
public final class CatalogViews {

    private CatalogViews() {
    }

    public record BodyZoneView(String id, String label, CodeLabel region) {
    }

    public record PainOptionsView(List<CodeLabel> qualities, List<CodeLabel> aggravators,
                                  List<CodeLabel> relievers, List<CodeLabel> distributions) {
    }

    /** @param roots roots whose myotome includes the movement, in anatomical order */
    public record StrengthTestView(String id, String name, String instructions, String safetyNote,
                                   CodeLabel motorAction, List<String> roots) {
    }
}
