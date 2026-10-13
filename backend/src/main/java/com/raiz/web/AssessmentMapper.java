package com.raiz.web;

import com.raiz.domain.assessment.Assessment;
import com.raiz.domain.assessment.Evidence;
import com.raiz.domain.assessment.PainProfile;
import com.raiz.domain.assessment.PatternInsight;
import com.raiz.domain.assessment.Referral;
import com.raiz.domain.assessment.RootMatch;
import com.raiz.domain.assessment.Specialist;
import com.raiz.domain.assessment.SymptomReport;
import com.raiz.domain.model.Sided;
import com.raiz.web.dto.AssessmentRequest;
import com.raiz.web.dto.AssessmentResponse;
import com.raiz.web.dto.AssessmentResponse.EvidenceView;
import com.raiz.web.dto.AssessmentResponse.PatternView;
import com.raiz.web.dto.AssessmentResponse.ReferralView;
import com.raiz.web.dto.AssessmentResponse.RootMatchView;
import com.raiz.web.dto.CodeLabel;
import com.raiz.web.dto.RedFlagView;

import java.util.Set;
import java.util.stream.Collectors;

/** Traduce entre los DTOs de la API y el dominio. No toma decisiones clínicas. */
final class AssessmentMapper {

    private AssessmentMapper() {
    }

    static SymptomReport toReport(AssessmentRequest request) {
        return new SymptomReport(request.redFlags(), toPain(request.pain()),
                sided(request.sensoryZones()), sided(request.weakness()), sided(request.diminishedReflexes()));
    }

    private static PainProfile toPain(AssessmentRequest.Pain pain) {
        if (pain == null) {
            return null;
        }
        return new PainProfile(pain.intensity(), pain.durationDays(), pain.distribution(),
                pain.qualities(), pain.aggravators(), pain.relievers());
    }

    private static <T> Set<Sided<T>> sided(Set<AssessmentRequest.SidedFinding<T>> findings) {
        if (findings == null) {
            return null;
        }
        return findings.stream().map(f -> new Sided<>(f.finding(), f.side())).collect(Collectors.toSet());
    }

    /** switch exhaustivo: si el dominio agrega un caso a Assessment, esto deja de compilar. */
    static AssessmentResponse toResponse(Assessment assessment) {
        ReferralView referral = toView(assessment.referral());
        PatternView pattern = toView(assessment.pattern());
        return switch (assessment) {
            case Assessment.UrgentReferral u -> new AssessmentResponse.UrgentReferral(referral, pattern,
                    RedFlagView.sorted(u.reported()), RedFlagView.sorted(u.inferred()));
            case Assessment.RootCorrelation c -> new AssessmentResponse.RootCorrelation(referral, pattern,
                    c.matches().stream().map(AssessmentMapper::toView).toList());
            case Assessment.NoRootCorrelation n -> new AssessmentResponse.NoRootCorrelation(referral, pattern, n.note());
        };
    }

    private static ReferralView toView(Referral referral) {
        return new ReferralView(CodeLabel.of(referral.urgency(), referral.urgency().label()),
                referral.specialists().stream().map(s -> CodeLabel.of(s, s.label())).toList(),
                referral.message(), referral.disclaimer());
    }

    private static PatternView toView(PatternInsight pattern) {
        var chronicity = pattern.chronicity() == null ? null
                : CodeLabel.of(pattern.chronicity(), pattern.chronicity().label());
        return new PatternView(pattern.type(), chronicity, pattern.features(), pattern.explanation());
    }

    private static RootMatchView toView(RootMatch match) {
        return new RootMatchView(match.root().code(), match.side(), match.label(), match.score(),
                match.hasNeurologicalDeficit(), match.evidence().stream().map(AssessmentMapper::toView).toList());
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
