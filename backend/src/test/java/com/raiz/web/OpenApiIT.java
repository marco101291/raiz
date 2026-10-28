package com.raiz.web;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The frontend and Alivia generate their types from this document: the contract must be complete. */
class OpenApiIT extends ApiIntegrationTest {

    private static final String SCHEMAS = "$.components.schemas";
    private static final String DISCRIMINATOR = "$.components.schemas.AssessmentResponse.discriminator";
    private static final String ASSESS_200 = "$.paths['/api/v1/assessments'].post.responses['200'].content";

    @Test
    void assessmentResponseDocumentsTheTypeDiscriminator() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DISCRIMINATOR + ".propertyName").value("type"))
                .andExpect(jsonPath(DISCRIMINATOR + ".mapping.URGENT_REFERRAL")
                        .value("#/components/schemas/UrgentReferral"))
                .andExpect(jsonPath(DISCRIMINATOR + ".mapping.ROOT_CORRELATION")
                        .value("#/components/schemas/RootCorrelation"))
                .andExpect(jsonPath(DISCRIMINATOR + ".mapping.NO_ROOT_CORRELATION")
                        .value("#/components/schemas/NoRootCorrelation"));
    }

    @Test
    void nonNullFieldsAreRequired() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(SCHEMAS + ".CodeLabel.required", containsInAnyOrder("id", "label")))
                .andExpect(jsonPath(SCHEMAS + ".RootCorrelation.allOf[1].required",
                        containsInAnyOrder("referral", "pattern", "matches", "otherCauses")))
                .andExpect(jsonPath(SCHEMAS + ".PatternView.required",
                        containsInAnyOrder("type", "features", "explanation")));
    }

    @Test
    void nullableFieldsAreOptionalAndAcceptNull() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(SCHEMAS + ".PatternView.properties.chronicity.oneOf[0]['$ref']")
                        .value("#/components/schemas/CodeLabel"))
                .andExpect(jsonPath(SCHEMAS + ".PatternView.properties.chronicity.oneOf[1].type").value("null"))
                .andExpect(jsonPath(SCHEMAS + ".PatientView.allOf[1].properties.patientSummary.type",
                        containsInAnyOrder("string", "null")))
                .andExpect(jsonPath(SCHEMAS + ".AssessmentRequest.required").doesNotExist())
                .andExpect(jsonPath(SCHEMAS + ".History.required", contains("priorHerniation")));
    }

    @Test
    void responsesAreDocumentedAsJson() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(ASSESS_200 + "['application/json']").exists())
                .andExpect(jsonPath(ASSESS_200 + "['*/*']").doesNotExist());
    }
}
