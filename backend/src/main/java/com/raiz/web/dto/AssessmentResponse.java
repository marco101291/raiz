package com.raiz.web.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.raiz.domain.assessment.PatternInsight;
import com.raiz.domain.model.Side;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Result of an assessment. The {@code type} field tells the three cases apart; all carry
 * {@code referral} and {@code pattern}, like the domain's sealed interface.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AssessmentResponse.UrgentReferral.class, name = "URGENT_REFERRAL"),
        @JsonSubTypes.Type(value = AssessmentResponse.RootCorrelation.class, name = "ROOT_CORRELATION"),
        @JsonSubTypes.Type(value = AssessmentResponse.NoRootCorrelation.class, name = "NO_ROOT_CORRELATION")
})
// Jackson uses @JsonSubTypes to serialize; OpenAPI needs the explicit mapping so generated clients
// (openapi-typescript) know which schema belongs to each "type" value.
@Schema(discriminatorProperty = "type", discriminatorMapping = {
        @DiscriminatorMapping(value = "URGENT_REFERRAL", schema = AssessmentResponse.UrgentReferral.class),
        @DiscriminatorMapping(value = "ROOT_CORRELATION", schema = AssessmentResponse.RootCorrelation.class),
        @DiscriminatorMapping(value = "NO_ROOT_CORRELATION", schema = AssessmentResponse.NoRootCorrelation.class)
})
public sealed interface AssessmentResponse {

    ReferralView referral();

    PatternView pattern();

    /** @param inferred red flags detected by the engine (e.g. symptoms in both legs), not by the person */
    record UrgentReferral(ReferralView referral, PatternView pattern,
                          List<RedFlagView> reported, List<RedFlagView> inferred) implements AssessmentResponse {
    }

    /** @param otherCauses causes other than a root that also explain the marked zones */
    record RootCorrelation(ReferralView referral, PatternView pattern,
                           List<RootMatchView> matches, List<OtherCauseView> otherCauses) implements AssessmentResponse {
    }

    record NoRootCorrelation(ReferralView referral, PatternView pattern,
                             String note, List<OtherCauseView> otherCauses) implements AssessmentResponse {
    }

    record OtherCauseView(String id, String title, String description, String sourceReference) {
    }

    record ReferralView(CodeLabel urgency, List<CodeLabel> specialists, String message, String disclaimer) {
    }

    /** @param chronicity null when there was no pain questionnaire */
    record PatternView(PatternInsight.Type type, @Nullable CodeLabel chronicity, List<String> features,
                       String explanation) {
    }

    /** @param priorHerniationLevels levels of the person's prior herniation that typically compress this root */
    record RootMatchView(String rootCode, Side side, String label, int score, boolean neurologicalDeficit,
                         List<EvidenceView> evidence, List<String> priorHerniationLevels) {
    }

    record EvidenceView(Kind kind, String finding, int weight, String explanation) {

        public enum Kind { SENSORY, MOTOR, REFLEX }
    }
}
