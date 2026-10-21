package com.raiz.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssessmentApiIT extends ApiIntegrationTest {

    private static final String URI = "/api/v1/assessments";

    /** El ejemplo de la sección "API (v1)" del README. */
    private static final String README_EXAMPLE = """
            {
              "redFlags": [],
              "pain": {
                "intensity": 4,
                "durationDays": 9,
                "distribution": "PROXIMAL",
                "qualities": ["BURNING", "TINGLING"],
                "aggravators": ["SITTING", "FORWARD_BENDING"],
                "relievers": ["LYING_DOWN"]
              },
              "sensoryZones": [
                { "finding": "GROIN", "side": "RIGHT" },
                { "finding": "UPPER_ANTERIOR_THIGH", "side": "RIGHT" }
              ],
              "weakness": [],
              "diminishedReflexes": []
            }
            """;

    // --- Regla del README: ninguna respuesta de evaluación sin referral ---

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"redFlags\": [\"FEVER\"]}",
            "{\"sensoryZones\": [{\"finding\": \"THUMB\", \"side\": \"LEFT\"}]}",
            "{\"pain\": {\"intensity\": 2, \"durationDays\": 200, \"distribution\": \"AXIAL_ONLY\"}}",
            README_EXAMPLE
    })
    void everyAssessmentCarriesReferralPatternAndDisclaimer(String body) throws Exception {
        postJson(URI, body)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").isNotEmpty())
                .andExpect(jsonPath("$.referral.urgency.id").isNotEmpty())
                .andExpect(jsonPath("$.referral.specialists", not(empty())))
                .andExpect(jsonPath("$.referral.disclaimer").isNotEmpty())
                .andExpect(jsonPath("$.pattern.type").isNotEmpty());
    }

    // --- Los tres tipos de respuesta ---

    @Test
    void readmeExampleCorrelatesWithHighLumbarRoots() throws Exception {
        postJson(URI, README_EXAMPLE)
                .andExpect(jsonPath("$.type").value("ROOT_CORRELATION"))
                .andExpect(jsonPath("$.pattern.type").value("RADICULAR"))
                .andExpect(jsonPath("$.pattern.chronicity.id").value("ACUTE"))
                .andExpect(jsonPath("$.matches[*].label", contains("L2 derecha", "L1 derecha")))
                .andExpect(jsonPath("$.matches[0].score").value(4))
                .andExpect(jsonPath("$.matches[0].neurologicalDeficit").value(false))
                .andExpect(jsonPath("$.referral.urgency.id").value("ROUTINE"));
    }

    @Test
    void weaknessIsMotorEvidenceAndEscalatesToPrompt() throws Exception {
        postJson(URI, """
                {
                  "sensoryZones": [
                    { "finding": "SOLE", "side": "RIGHT" },
                    { "finding": "POSTERIOR_CALF", "side": "RIGHT" }
                  ],
                  "weakness": [{ "finding": "ANKLE_PLANTAR_FLEXION", "side": "RIGHT" }]
                }
                """)
                .andExpect(jsonPath("$.type").value("ROOT_CORRELATION"))
                .andExpect(jsonPath("$.matches[0].label").value("S1 derecha"))
                .andExpect(jsonPath("$.matches[0].score").value(7))
                .andExpect(jsonPath("$.matches[0].neurologicalDeficit").value(true))
                .andExpect(jsonPath("$.matches[0].evidence[?(@.kind == 'MOTOR')].finding")
                        .value(contains("ANKLE_PLANTAR_FLEXION")))
                .andExpect(jsonPath("$.referral.urgency.id").value("PROMPT"));
    }

    @Test
    void reportedRedFlagSkipsEverythingElse() throws Exception {
        postJson(URI, """
                { "redFlags": ["SADDLE_ANESTHESIA"], "sensoryZones": [{ "finding": "SOLE", "side": "RIGHT" }] }
                """)
                .andExpect(jsonPath("$.type").value("URGENT_REFERRAL"))
                .andExpect(jsonPath("$.referral.urgency.id").value("EMERGENCY_NOW"))
                .andExpect(jsonPath("$.reported[*].id", contains("SADDLE_ANESTHESIA")))
                .andExpect(jsonPath("$.inferred", empty()))
                .andExpect(jsonPath("$.pattern.type").value("NOT_ASSESSED"))
                .andExpect(jsonPath("$.matches").doesNotExist());
    }

    @Test
    void findingsOnBothLegsInferBilateralRedFlag() throws Exception {
        postJson(URI, """
                { "sensoryZones": [{ "finding": "SOLE", "side": "RIGHT" }, { "finding": "SOLE", "side": "LEFT" }] }
                """)
                .andExpect(jsonPath("$.type").value("URGENT_REFERRAL"))
                .andExpect(jsonPath("$.reported", empty()))
                .andExpect(jsonPath("$.inferred[*].id", contains("BILATERAL_SYMPTOMS")))
                .andExpect(jsonPath("$.referral.urgency.id").value("EMERGENCY_NOW"))
                .andExpect(jsonPath("$.otherCauses").doesNotExist());
    }

    @Test
    void groinOnBothSidesIsPromptAndListsOtherCauses() throws Exception {
        postJson(URI, """
                { "sensoryZones": [{ "finding": "GROIN", "side": "RIGHT" }, { "finding": "GROIN", "side": "LEFT" }] }
                """)
                .andExpect(jsonPath("$.type").value("ROOT_CORRELATION"))
                .andExpect(jsonPath("$.referral.urgency.id").value("PROMPT"))
                .andExpect(jsonPath("$.referral.message", startsWith("Sientes síntomas en la ingle de ambos lados.")))
                .andExpect(jsonPath("$.otherCauses[*].id",
                        contains("PUBALGIA", "HIP", "GROIN_NERVE_ENTRAPMENT", "REFERRED_LOW_LUMBAR")));
    }

    @Test
    void otherCauseSharedByMarkedZonesAppearsOnce() throws Exception {
        postJson(URI, """
                { "sensoryZones": [{ "finding": "THUMB", "side": "RIGHT" }, { "finding": "INDEX_FINGER", "side": "RIGHT" },
                                   { "finding": "MIDDLE_FINGER", "side": "RIGHT" }] }
                """)
                .andExpect(jsonPath("$.otherCauses[*].id", contains("CARPAL_TUNNEL")))
                .andExpect(jsonPath("$.otherCauses[0].sourceReference").isNotEmpty());
    }

    @Test
    void zonesWithoutOtherCausesReturnAnEmptyList() throws Exception {
        postJson(URI, """
                { "sensoryZones": [{ "finding": "SOLE", "side": "RIGHT" }] }
                """)
                .andExpect(jsonPath("$.otherCauses", empty()));
    }

    @Test
    void axialDiscogenicPainHasNoRootCorrelation() throws Exception {
        postJson(URI, """
                {
                  "pain": {
                    "intensity": 4, "durationDays": 10, "distribution": "AXIAL_ONLY",
                    "qualities": ["DULL_ACHE"], "aggravators": ["SITTING", "COUGH_SNEEZE"], "relievers": ["WALKING"]
                  }
                }
                """)
                .andExpect(jsonPath("$.type").value("NO_ROOT_CORRELATION"))
                .andExpect(jsonPath("$.pattern.type").value("DISCOGENIC"))
                .andExpect(jsonPath("$.pattern.features", hasSize(3)))
                .andExpect(jsonPath("$.note").isNotEmpty());
    }

    // --- Errores: 400 con ProblemDetail, nunca 500 ---

    @Test
    void outOfRangeIntensityNamesTheField() throws Exception {
        postJson(URI, """
                { "pain": { "intensity": 11, "durationDays": 1, "distribution": "AXIAL_ONLY" } }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Datos no válidos"))
                .andExpect(jsonPath("$.errors[*].field", contains("pain.intensity")))
                .andExpect(jsonPath("$.errors[*].message", contains("debe estar entre 0 y 10")));
    }

    @Test
    void missingRequiredPainFieldsAreListed() throws Exception {
        postJson(URI, "{ \"pain\": { \"intensity\": 3 } }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpect(jsonPath("$.errors[*].message", everyItem(org.hamcrest.Matchers.is("es obligatoria"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"redFlags\": [null]}",
            "{\"sensoryZones\": [null]}",
            "{\"sensoryZones\": [{\"finding\": \"GREAT_TOE\"}]}",
            "{\"redFlags\": [\"NOT_A_FLAG\"]}",
            "{\"sensoryZones\": [{\"finding\": \"ELBOW\", \"side\": \"RIGHT\"}]}",
            "{ not json"
    })
    void malformedInputIs400NotServerError(String body) throws Exception {
        postJson(URI, body)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }
}
