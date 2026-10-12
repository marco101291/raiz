package com.raiz.domain.assessment;

import com.raiz.domain.model.NerveRoot;
import com.raiz.domain.model.RedFlag;
import com.raiz.domain.model.Side;
import com.raiz.domain.model.Sided;
import com.raiz.domain.port.NerveRootCatalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.raiz.domain.assessment.Specialist.*;

/**
 * Motor de reglas educativo.
 *
 * <ol>
 *   <li>Triage: cualquier dato de alarma, reportado o inferido (síntomas en ambos lados), corta
 *       la evaluación.</li>
 *   <li>Patrón de dolor: lectura del cuestionario ("¿es una hernia?").</li>
 *   <li>Correlación: cada raíz, por lado, suma puntos por zona sensitiva (2), debilidad (3) y reflejo (3)
 *       que coincidan con su territorio.</li>
 *   <li>Derivación: siempre presente, con urgencia según los hallazgos.</li>
 * </ol>
 *
 * No diagnostica. Los territorios radiculares se solapan y varían entre personas; el
 * resultado indica qué raíces son compatibles, no cuál está afectada.
 */
public final class CorrelationEngine {

    static final int SEVERE_PAIN_THRESHOLD = 8;

    private final NerveRootCatalog catalog;
    private final PainPatternClassifier classifier = new PainPatternClassifier();

    public CorrelationEngine(NerveRootCatalog catalog) {
        this.catalog = catalog;
    }

    public Assessment assess(SymptomReport report) {
        Set<RedFlag> inferred = inferRedFlags(report);
        if (!report.redFlags().isEmpty() || !inferred.isEmpty()) {
            return triage(report.redFlags(), inferred);
        }

        PatternInsight pattern = classifier.classify(report);

        if (!report.hasNeurologicalFindings()) {
            return noRootCorrelation(pattern, report);
        }

        List<NerveRoot> roots = catalog.findAll();
        List<RootMatch> matches = roots.stream()
                .flatMap(root -> Arrays.stream(Side.values())
                        .map(side -> new RootMatch(root, side, evidenceFor(root, side, report))))
                .filter(match -> match.score() > 0)
                .sorted(Comparator.comparingInt(RootMatch::score).reversed()
                        .thenComparingInt(match -> roots.indexOf(match.root()))
                        .thenComparing(RootMatch::side))
                .toList();

        if (matches.isEmpty()) {
            return new Assessment.NoRootCorrelation(pattern,
                    "Los síntomas reportados no coinciden con un territorio radicular del catálogo.",
                    new Referral(Urgency.ROUTINE, List.of(PHYSICIAN, PHYSIATRIST),
                            "Un profesional puede explorar otras causas."));
        }

        return new Assessment.RootCorrelation(pattern, matches, referralFor(matches, pattern, report));
    }

    /** Síntomas en ambos lados de una misma región cuentan como dato de alarma. */
    private static Set<RedFlag> inferRedFlags(SymptomReport report) {
        if (!report.bilateralRegions().isEmpty() && !report.redFlags().contains(RedFlag.BILATERAL_SYMPTOMS)) {
            return Set.of(RedFlag.BILATERAL_SYMPTOMS);
        }
        return Set.of();
    }

    private static List<Evidence> evidenceFor(NerveRoot root, Side side, SymptomReport report) {
        var evidence = new ArrayList<Evidence>();
        onSide(report.sensoryZones(), side)
                .filter(root.dermatome()::contains)
                .map(Evidence.SensoryMatch::new)
                .forEach(evidence::add);
        onSide(report.weakness(), side)
                .filter(root.myotome()::contains)
                .map(Evidence.MotorMatch::new)
                .forEach(evidence::add);
        onSide(report.diminishedReflexes(), side)
                .filter(root.reflexes()::contains)
                .map(Evidence.ReflexMatch::new)
                .forEach(evidence::add);
        return evidence;
    }

    private static <T> Stream<T> onSide(Set<Sided<T>> findings, Side side) {
        return findings.stream().filter(f -> f.side() == side).map(Sided::finding);
    }

    private static Assessment triage(Set<RedFlag> reported, Set<RedFlag> inferred) {
        boolean emergency = Stream.concat(reported.stream(), inferred.stream()).anyMatch(RedFlag::emergency);
        Referral referral = emergency
                ? new Referral(Urgency.EMERGENCY_NOW, List.of(EMERGENCY_ROOM, SPINE_SURGEON),
                        "Estos síntomas pueden indicar compresión grave de la médula o de la cola de "
                                + "caballo. No esperes: acude a urgencias.")
                : new Referral(Urgency.PROMPT, List.of(PHYSICIAN, SPINE_SURGEON),
                        "Estos datos obligan a descartar causas distintas a una hernia (infección, "
                                + "fractura, tumor) antes de cualquier tratamiento.");
        return new Assessment.UrgentReferral(reported, inferred, referral);
    }

    private static Assessment noRootCorrelation(PatternInsight pattern, SymptomReport report) {
        if (pattern.type() == PatternInsight.Type.RADICULAR) {
            return new Assessment.NoRootCorrelation(pattern,
                    "Describes dolor de tipo nervioso que baja por la extremidad. Marca en el mapa dónde "
                            + "lo sientes para ver qué raíces son compatibles.",
                    severePain(report)
                            ? severeRadicularReferral()
                            : new Referral(Urgency.ROUTINE, List.of(PHYSIATRIST, PHYSIOTHERAPIST, SPINE_SURGEON),
                                    "Un profesional puede confirmar qué raíz está irritada."));
        }
        boolean chronic = pattern.chronicity() == PainProfile.Chronicity.CHRONIC;
        return new Assessment.NoRootCorrelation(pattern,
                "Lo que describes no sigue el territorio de una raíz nerviosa.",
                new Referral(Urgency.ROUTINE,
                        chronic ? List.of(PHYSIATRIST, PHYSIOTHERAPIST) : List.of(PHYSIOTHERAPIST, PHYSIATRIST),
                        chronic
                                ? "Llevas más de 12 semanas con dolor: conviene una valoración integral, no "
                                        + "solo de la espalda."
                                : "Si el dolor no mejora en 2 a 3 semanas, o aparece irradiación, consulta."));
    }

    private static Referral referralFor(List<RootMatch> matches, PatternInsight pattern, SymptomReport report) {
        if (matches.stream().anyMatch(RootMatch::hasNeurologicalDeficit)) {
            return new Referral(Urgency.PROMPT, List.of(SPINE_SURGEON, NEUROLOGIST),
                    "Hay datos compatibles con pérdida de fuerza o de reflejos. Conviene una exploración "
                            + "neurológica pronto y vigilar que no avance.");
        }
        if (severePain(report)) {
            return severeRadicularReferral();
        }
        if (pattern.chronicity() == PainProfile.Chronicity.CHRONIC) {
            return new Referral(Urgency.ROUTINE, List.of(SPINE_SURGEON, PHYSIATRIST),
                    "Los síntomas nerviosos llevan más de 12 semanas. Conviene una valoración especializada "
                            + "que incluya estudios de imagen y, si aplica, opinión quirúrgica.");
        }
        return new Referral(Urgency.ROUTINE, List.of(PHYSIATRIST, PHYSIOTHERAPIST, SPINE_SURGEON),
                "Los síntomas sensitivos sin pérdida de fuerza suelen mejorar con tratamiento "
                        + "conservador. Un profesional debe confirmar el nivel y guiar el tratamiento.");
    }

    private static boolean severePain(SymptomReport report) {
        return report.painProfile().map(p -> p.intensity() >= SEVERE_PAIN_THRESHOLD).orElse(false);
    }

    private static Referral severeRadicularReferral() {
        return new Referral(Urgency.PROMPT, List.of(PHYSICIAN, SPINE_SURGEON),
                "El dolor es intenso. Busca valoración médica en los próximos días para controlarlo; "
                        + "el dolor mal controlado retrasa la recuperación.");
    }
}
