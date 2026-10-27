package com.raiz.web;

import com.raiz.domain.assessment.Assessment;
import com.raiz.domain.assessment.Evidence;
import com.raiz.domain.assessment.MedicalHistory;
import com.raiz.domain.assessment.PainProfile;
import com.raiz.domain.assessment.PatternInsight;
import com.raiz.domain.assessment.Referral;
import com.raiz.domain.assessment.RootMatch;
import com.raiz.domain.assessment.Specialist;
import com.raiz.domain.assessment.SymptomReport;
import com.raiz.domain.model.DiscLevel;
import com.raiz.domain.model.PainDistribution;
import com.raiz.domain.model.Sided;
import com.raiz.domain.model.SpinalRegion;
import com.raiz.persistence.OtherCause;
import com.raiz.persistence.ResultTextCatalog;
import com.raiz.web.dto.AssessmentRequest;
import com.raiz.web.dto.AssessmentResponse;
import com.raiz.web.dto.AssessmentResponse.EvidenceView;
import com.raiz.web.dto.AssessmentResponse.OtherCauseView;
import com.raiz.web.dto.AssessmentResponse.PatternView;
import com.raiz.web.dto.AssessmentResponse.ReferralView;
import com.raiz.web.dto.AssessmentResponse.RootMatchView;
import com.raiz.web.dto.CodeLabel;
import com.raiz.web.dto.RedFlagView;

import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Translates between the API DTOs and the domain. It makes no clinical decisions. */
final class AssessmentMapper {

    private AssessmentMapper() {
    }

    static SymptomReport toReport(AssessmentRequest request) {
        return new SymptomReport(request.redFlags(), toPain(request.pain()),
                sided(request.sensoryZones()), sided(request.weakness()), sided(request.diminishedReflexes()),
                toHistory(request.history()));
    }

    private static MedicalHistory toHistory(AssessmentRequest.History history) {
        if (history == null) {
            return MedicalHistory.NONE;
        }
        return new MedicalHistory(history.priorHerniation(), history.herniationLevels(),
                Boolean.TRUE.equals(history.priorSpineSurgery()));
    }

    private static PainProfile toPain(AssessmentRequest.Pain pain) {
        if (pain == null) {
            return null;
        }
        // A repeated region is a client error (400), not a server error: hence IllegalArgumentException.
        var reach = pain.reach().stream().collect(Collectors.toMap(
                AssessmentRequest.Reach::region, AssessmentRequest.Reach::distribution,
                (first, second) -> {
                    throw new IllegalArgumentException("Cada región solo puede aparecer una vez en pain.reach");
                },
                () -> new EnumMap<SpinalRegion, PainDistribution>(SpinalRegion.class)));
        return new PainProfile(pain.intensity(), pain.durationDays(), reach,
                pain.qualities(), pain.aggravators(), pain.relievers());
    }

    private static <T> Set<Sided<T>> sided(Set<AssessmentRequest.SidedFinding<T>> findings) {
        if (findings == null) {
            return null;
        }
        return findings.stream().map(f -> new Sided<>(f.finding(), f.side())).collect(Collectors.toSet());
    }

    /**
     * Exhaustive switch: if the domain adds a case to Assessment, this stops compiling.
     *
     * @param otherCauses omitted on an urgent referral: there the only thing that matters is going to the ER
     * @param texts       turns the engine's reasons into text for the person
     */
    static AssessmentResponse toResponse(Assessment assessment, List<OtherCause> otherCauses,
                                         ResultTextCatalog texts) {
        ReferralView referral = toView(assessment.referral(), texts);
        PatternView pattern = toView(assessment.pattern(), texts);
        List<OtherCauseView> causes = otherCauses.stream()
                .map(c -> new OtherCauseView(c.id(), c.title(), c.description(), c.sourceReference()))
                .toList();
        return switch (assessment) {
            case Assessment.UrgentReferral u -> new AssessmentResponse.UrgentReferral(referral, pattern,
                    RedFlagView.sorted(u.reported()), RedFlagView.sorted(u.inferred()));
            case Assessment.RootCorrelation c -> new AssessmentResponse.RootCorrelation(referral, pattern,
                    c.matches().stream().map(AssessmentMapper::toView).toList(), causes);
            case Assessment.NoRootCorrelation n ->
                    new AssessmentResponse.NoRootCorrelation(referral, pattern, texts.note(n.note()), causes);
        };
    }

    /** The reasons are joined into a single message, in the order the engine gave them. */
    private static ReferralView toView(Referral referral, ResultTextCatalog texts) {
        String message = referral.reasons().stream().map(texts::referral).collect(Collectors.joining(" "));
        return new ReferralView(CodeLabel.of(referral.urgency(), referral.urgency().label()),
                referral.specialists().stream().map(s -> CodeLabel.of(s, s.label())).toList(),
                message, texts.disclaimer());
    }

    private static PatternView toView(PatternInsight pattern, ResultTextCatalog texts) {
        var chronicity = pattern.chronicity() == null ? null
                : CodeLabel.of(pattern.chronicity(), pattern.chronicity().label());
        return new PatternView(pattern.type(), chronicity, pattern.features(), texts.pattern(pattern.type()));
    }

    private static RootMatchView toView(RootMatch match) {
        return new RootMatchView(match.root().code(), match.side(), match.label(), match.score(),
                match.hasNeurologicalDeficit(), match.evidence().stream().map(AssessmentMapper::toView).toList(),
                match.priorHerniationLevels().stream().sorted().map(DiscLevel::label).toList());
    }

    private static EvidenceView toView(Evidence evidence) {
        return switch (evidence) {
            case Evidence.SensoryMatch s -> new EvidenceView(EvidenceView.Kind.SENSORY, s.zone().name(),
                    s.weight(), s.explanation());
            case Evidence.MotorMatch m -> new EvidenceView(EvidenceView.Kind.MOTOR, m.action().name(),
                    m.weight(), m.explanation());
            case Evidence.ReflexMatch r -> new EvidenceView(EvidenceView.Kind.REFLEX, r.reflex().name(),
                    r.weight(), r.explanation());
        };
    }
}
