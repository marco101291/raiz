package com.raiz.domain.assessment;

import com.raiz.domain.assessment.Assessment.NoRootCorrelation.Note;
import com.raiz.domain.assessment.ReferralReason.Kind;
import com.raiz.domain.model.NerveRoot;
import com.raiz.domain.model.RedFlag;
import com.raiz.domain.model.Side;
import com.raiz.domain.model.Sided;
import com.raiz.domain.model.SpinalRegion;
import com.raiz.domain.port.CommonBilateralCauses;
import com.raiz.domain.port.NerveRootCatalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.raiz.domain.assessment.Specialist.*;

/**
 * Educational rule engine.
 *
 * <ol>
 *   <li>Triage: any red flag, reported or inferred (symptoms on both sides), stops the assessment.
 *       If the bilateral findings are only in zones with common, non-urgent causes
 *       ({@link CommonBilateralCauses}), it does not stop: the assessment goes on and asks for a prompt
 *       visit, listing the signs that do require the emergency room.</li>
 *   <li>Pain pattern: reading of the questionnaire ("is it a herniation?").</li>
 *   <li>Correlation: each root, per side, scores points for each sensory zone (2), weakness (3) and
 *       reflex (3) that matches its territory.</li>
 *   <li>Referral: always present, with urgency based on the findings. It comes from an ordered list of
 *       rules where the first one that applies wins.</li>
 * </ol>
 *
 * The engine decides, it does not write: it returns reasons ({@link ReferralReason}, {@link Note}) and the
 * text for the person lives in the catalog. Tests therefore check decisions, not sentences.
 *
 * It does not diagnose. Root territories overlap and vary between people; the result says which roots are
 * compatible, not which one is affected.
 */
public final class CorrelationEngine {

    static final int SEVERE_PAIN_THRESHOLD = 8;

    /** What the referral rules can look at. {@code matches} is empty when there was no correlation. */
    private record Findings(SymptomReport report, PatternInsight pattern, List<RootMatch> matches) {

        boolean neurologicalDeficit() {
            return matches.stream().anyMatch(RootMatch::hasNeurologicalDeficit);
        }

        boolean severePain() {
            return report.painProfile().map(p -> p.intensity() >= SEVERE_PAIN_THRESHOLD).orElse(false);
        }

        boolean chronic() {
            return pattern.chronicity() == PainProfile.Chronicity.CHRONIC;
        }

        boolean radicular() {
            return pattern.type() == PatternInsight.Type.RADICULAR;
        }
    }

    /** When {@code applies} holds, this is the referral. */
    private record Rule(Predicate<Findings> applies, Urgency urgency, List<Specialist> specialists, Kind reason) {

        Referral referral() {
            return new Referral(urgency, specialists, List.of(ReferralReason.of(reason)));
        }
    }

    private static final Predicate<Findings> OTHERWISE = findings -> true;

    /** There are compatible roots. Loss of function first, then pain, then duration. */
    private static final List<Rule> ROOT_CORRELATION_RULES = List.of(
            new Rule(Findings::neurologicalDeficit, Urgency.PROMPT, List.of(SPINE_SURGEON, NEUROLOGIST),
                    Kind.NEUROLOGICAL_DEFICIT),
            new Rule(Findings::severePain, Urgency.PROMPT, List.of(PHYSICIAN, SPINE_SURGEON), Kind.SEVERE_PAIN),
            new Rule(Findings::chronic, Urgency.ROUTINE, List.of(SPINE_SURGEON, PHYSIATRIST), Kind.CHRONIC_RADICULAR),
            new Rule(OTHERWISE, Urgency.ROUTINE, List.of(PHYSIATRIST, PHYSIOTHERAPIST, SPINE_SURGEON),
                    Kind.SENSORY_ONLY));

    /** No zones, weakness or reflexes: only the pain questionnaire, if any. */
    private static final List<Rule> PAIN_ONLY_RULES = List.of(
            new Rule(f -> f.radicular() && f.severePain(), Urgency.PROMPT, List.of(PHYSICIAN, SPINE_SURGEON),
                    Kind.SEVERE_PAIN),
            new Rule(Findings::radicular, Urgency.ROUTINE, List.of(PHYSIATRIST, PHYSIOTHERAPIST, SPINE_SURGEON),
                    Kind.RADICULAR_UNMAPPED),
            new Rule(Findings::chronic, Urgency.ROUTINE, List.of(PHYSIATRIST, PHYSIOTHERAPIST), Kind.CHRONIC_PAIN),
            new Rule(OTHERWISE, Urgency.ROUTINE, List.of(PHYSIOTHERAPIST, PHYSIATRIST), Kind.SELF_LIMITED));

    private static final Rule NO_TERRITORY_MATCH_RULE =
            new Rule(OTHERWISE, Urgency.ROUTINE, List.of(PHYSICIAN, PHYSIATRIST), Kind.NO_TERRITORY_MATCH);

    private final NerveRootCatalog catalog;
    private final CommonBilateralCauses commonBilateralCauses;
    private final PainPatternClassifier classifier = new PainPatternClassifier();

    public CorrelationEngine(NerveRootCatalog catalog, CommonBilateralCauses commonBilateralCauses) {
        this.catalog = catalog;
        this.commonBilateralCauses = commonBilateralCauses;
    }

    public Assessment assess(SymptomReport report) {
        Set<RedFlag> inferred = inferRedFlags(report);
        if (!report.redFlags().isEmpty() || !inferred.isEmpty()) {
            return triage(report.redFlags(), inferred);
        }

        PatternInsight pattern = classifier.classify(report);

        if (!report.hasNeurologicalFindings()) {
            var findings = new Findings(report, pattern, List.of());
            return new Assessment.NoRootCorrelation(pattern,
                    findings.radicular() ? Note.RADICULAR_UNMAPPED : Note.NOT_RADICULAR,
                    firstApplying(PAIN_ONLY_RULES, findings));
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
            return new Assessment.NoRootCorrelation(pattern, Note.NO_TERRITORY_MATCH,
                    withBilateralCaution(NO_TERRITORY_MATCH_RULE.referral(), report));
        }

        Referral referral = firstApplying(ROOT_CORRELATION_RULES, new Findings(report, pattern, matches));
        return new Assessment.RootCorrelation(pattern, matches, withBilateralCaution(referral, report));
    }

    private static Referral firstApplying(List<Rule> rules, Findings findings) {
        return rules.stream()
                .filter(rule -> rule.applies().test(findings))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("The last rule must always apply"))
                .referral();
    }

    /**
     * Symptoms on both sides of the same region count as a red flag, unless the bilateral findings are
     * only in zones with common, non-urgent causes (groin, median-nerve fingers).
     */
    private Set<RedFlag> inferRedFlags(SymptomReport report) {
        boolean alarming = !report.bilateralRegions(zone -> !commonBilateralCauses.existFor(zone)).isEmpty();
        if (alarming && !report.redFlags().contains(RedFlag.BILATERAL_SYMPTOMS)) {
            return Set.of(RedFlag.BILATERAL_SYMPTOMS);
        }
        return Set.of();
    }

    /**
     * Bilateral findings that did not reach a red flag (only zones with common causes) raise the referral to
     * "within the next few days" and put first, per region, the other causes and the signs that do require
     * the emergency room.
     */
    private static Referral withBilateralCaution(Referral referral, SymptomReport report) {
        Set<SpinalRegion> regions = report.bilateralRegions();
        if (regions.isEmpty()) {
            return referral;
        }
        var reasons = new ArrayList<ReferralReason>();
        regions.stream().sorted().map(ReferralReason::bilateralCommonCause).forEach(reasons::add);
        reasons.addAll(referral.reasons());

        var specialists = new LinkedHashSet<Specialist>();
        specialists.add(PHYSICIAN);
        specialists.addAll(referral.specialists());
        Urgency urgency = referral.urgency() == Urgency.ROUTINE ? Urgency.PROMPT : referral.urgency();
        return new Referral(urgency, List.copyOf(specialists), reasons);
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
                        List.of(ReferralReason.of(Kind.EMERGENCY_RED_FLAG)))
                : new Referral(Urgency.PROMPT, List.of(PHYSICIAN, SPINE_SURGEON),
                        List.of(ReferralReason.of(Kind.RED_FLAG_RULE_OUT)));
        return new Assessment.UrgentReferral(reported, inferred, referral);
    }
}
