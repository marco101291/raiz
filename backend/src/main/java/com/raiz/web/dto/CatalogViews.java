package com.raiz.web.dto;

import java.util.List;

/** Responses of {@code /api/v1/catalog/**}. */
public final class CatalogViews {

    private CatalogViews() {
    }

    public record BodyZoneView(String id, String label, CodeLabel region) {
    }

    /** @param regions where the pain can start, each with its own "how far does it reach" options */
    /** @param discLevels levels a person may know from an MRI report, for the history question */
    public record PainOptionsView(List<CodeLabel> qualities, List<CodeLabel> aggravators,
                                  List<CodeLabel> relievers, List<PainRegionView> regions,
                                  List<DiscLevelView> discLevels) {
    }

    public record DiscLevelView(String id, String label, String region) {
    }

    /** @param label plain-language region name ("Cuello", "Espalda baja") */
    public record PainRegionView(String id, String label, List<CodeLabel> distributions) {
    }

    /** @param roots roots whose myotome includes the movement, in anatomical order */
    public record StrengthTestView(String id, String name, String instructions, String safetyNote,
                                   CodeLabel motorAction, List<String> roots) {
    }

    /** @param roots roots whose myotome includes the movement, in anatomical order */
    public record FunctionalQuestionView(String id, String question, CodeLabel motorAction, List<String> roots) {
    }
}
