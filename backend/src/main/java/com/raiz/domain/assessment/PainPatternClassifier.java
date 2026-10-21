package com.raiz.domain.assessment;

import com.raiz.domain.model.Aggravator;
import com.raiz.domain.model.PainDistribution;
import com.raiz.domain.model.Reliever;

import java.util.ArrayList;
import java.util.List;

/**
 * Classifies the pain pattern from the questionnaire. Educational heuristic: the features suggest, they
 * do not confirm. Priority: radicular > discogenic > extension-sensitive > nonspecific.
 */
public final class PainPatternClassifier {

    private static final int DISCOGENIC_THRESHOLD = 2;

    public PatternInsight classify(SymptomReport report) {
        if (report.painProfile().isEmpty()) {
            return PatternInsight.notAssessed();
        }
        PainProfile pain = report.painProfile().get();
        var features = new ArrayList<String>();

        // Radicular: nerve-type pain that follows a limb.
        boolean followsLimb = pain.distribution() == PainDistribution.DISTAL || !report.sensoryZones().isEmpty();
        if (pain.hasNeuropathicQuality() && followsLimb) {
            pain.qualities().stream().filter(q -> q.neuropathic())
                    .forEach(q -> features.add("Dolor descrito como: " + q.label().toLowerCase()));
            features.add(pain.distribution() == PainDistribution.DISTAL
                    ? "Baja por debajo de la rodilla o del codo"
                    : "Sigue una zona concreta de la extremidad");
            return new PatternInsight(PatternInsight.Type.RADICULAR, pain.chronicity(), features);
        }

        // Discogenic: worse with whatever raises the pressure inside the disc.
        int discogenicScore = 0;
        for (Aggravator a : pain.aggravators()) {
            if (a.discogenicHint()) {
                discogenicScore++;
                features.add("Empeora al " + a.label().toLowerCase());
            }
        }
        if (pain.relievers().contains(Reliever.LYING_DOWN) || pain.relievers().contains(Reliever.WALKING)) {
            discogenicScore++;
            features.add("Mejora al acostarse o caminar suave");
        }
        if (discogenicScore >= DISCOGENIC_THRESHOLD) {
            return new PatternInsight(PatternInsight.Type.DISCOGENIC, pain.chronicity(), features);
        }

        // Extension-sensitive: typical of facet joints.
        features.clear();
        boolean extensionAggravated = pain.aggravators().contains(Aggravator.BACKWARD_BENDING)
                || pain.aggravators().contains(Aggravator.STANDING_LONG);
        if (extensionAggravated && pain.relievers().contains(Reliever.SITTING)) {
            features.add("Empeora al inclinarse hacia atrás o estar de pie");
            features.add("Mejora al sentarse");
            return new PatternInsight(PatternInsight.Type.EXTENSION_SENSITIVE, pain.chronicity(), features);
        }

        return new PatternInsight(PatternInsight.Type.NONSPECIFIC, pain.chronicity(), List.of());
    }
}
