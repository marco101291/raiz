package com.raiz.web.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.raiz.domain.assessment.PatternInsight;
import com.raiz.domain.model.Side;

import java.util.List;

/**
 * Resultado de una evaluación. El campo {@code type} distingue los tres casos; todos llevan
 * {@code referral} y {@code pattern}, igual que la sealed interface del dominio.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AssessmentResponse.UrgentReferral.class, name = "URGENT_REFERRAL"),
        @JsonSubTypes.Type(value = AssessmentResponse.RootCorrelation.class, name = "ROOT_CORRELATION"),
        @JsonSubTypes.Type(value = AssessmentResponse.NoRootCorrelation.class, name = "NO_ROOT_CORRELATION")
})
public sealed interface AssessmentResponse {

    ReferralView referral();

    PatternView pattern();

    /** @param inferred alarmas que detectó el motor (p. ej. síntomas en ambas piernas), no la persona */
    record UrgentReferral(ReferralView referral, PatternView pattern,
                          List<RedFlagView> reported, List<RedFlagView> inferred) implements AssessmentResponse {
    }

    record RootCorrelation(ReferralView referral, PatternView pattern,
                           List<RootMatchView> matches) implements AssessmentResponse {
    }

    record NoRootCorrelation(ReferralView referral, PatternView pattern,
                             String note) implements AssessmentResponse {
    }

    record ReferralView(CodeLabel urgency, List<CodeLabel> specialists, String message, String disclaimer) {
    }

    /** @param chronicity null cuando no hubo cuestionario de dolor */
    record PatternView(PatternInsight.Type type, CodeLabel chronicity, List<String> features, String explanation) {
    }

    record RootMatchView(String rootCode, Side side, String label, int score, boolean neurologicalDeficit,
                         List<EvidenceView> evidence) {
    }

    record EvidenceView(Kind kind, String finding, int weight, String explanation) {

        public enum Kind { SENSORY, MOTOR, REFLEX }
    }
}
