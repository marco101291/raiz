package com.raiz.web;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** El frontend y Alivia generan sus tipos de este documento: el contrato debe estar completo. */
class OpenApiIT extends ApiIntegrationTest {

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
    void responsesAreDocumentedAsJson() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(ASSESS_200 + "['application/json']").exists())
                .andExpect(jsonPath(ASSESS_200 + "['*/*']").doesNotExist());
    }
}
