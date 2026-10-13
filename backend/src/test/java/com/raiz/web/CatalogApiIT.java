package com.raiz.web;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CatalogApiIT extends ApiIntegrationTest {

    @Test
    void bodyZonesCarrySpanishLabelAndRegion() throws Exception {
        mvc.perform(get("/api/v1/catalog/body-zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(22)))
                .andExpect(jsonPath("$[?(@.id == 'GREAT_TOE')].label").value("Dedo gordo del pie"))
                .andExpect(jsonPath("$[?(@.id == 'GREAT_TOE')].region.id").value("LUMBOSACRAL"))
                .andExpect(jsonPath("$[?(@.id == 'THUMB')].region.id").value("CERVICAL"));
    }

    @Test
    void redFlagsSayWhichAreEmergencies() throws Exception {
        mvc.perform(get("/api/v1/catalog/red-flags"))
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[?(@.id == 'SADDLE_ANESTHESIA')].emergency").value(true))
                .andExpect(jsonPath("$[?(@.id == 'FEVER')].emergency").value(false));
    }

    @Test
    void painOptionsCoverEveryQuestionnaireField() throws Exception {
        mvc.perform(get("/api/v1/catalog/pain-options"))
                .andExpect(jsonPath("$.qualities", hasSize(7)))
                .andExpect(jsonPath("$.aggravators", hasSize(8)))
                .andExpect(jsonPath("$.relievers", hasSize(5)))
                .andExpect(jsonPath("$.distributions[*].id", contains("AXIAL_ONLY", "PROXIMAL", "DISTAL")))
                .andExpect(jsonPath("$.relievers[?(@.id == 'NOTHING')].label").value("Nada lo alivia"));
    }

    /** Las raíces de cada prueba salen del miotoma; deben coincidir con la tabla del README. */
    @Test
    void strengthTestRootsMatchReadmeTable() throws Exception {
        mvc.perform(get("/api/v1/catalog/strength-tests"))
                .andExpect(jsonPath("$[*].id", contains("HEEL_WALK", "TOE_WALK", "SINGLE_LEG_HEEL_RAISE",
                        "GREAT_TOE_RESISTANCE", "SINGLE_LEG_SIT_TO_STAND", "SINGLE_LEG_STANCE")))
                .andExpect(jsonPath("$[0].roots", contains("L4", "L5")))
                .andExpect(jsonPath("$[1].roots", contains("S1")))
                .andExpect(jsonPath("$[2].roots", contains("S1")))
                .andExpect(jsonPath("$[3].roots", contains("L5")))
                .andExpect(jsonPath("$[4].roots", contains("L3", "L4")))
                .andExpect(jsonPath("$[5].roots", contains("L5")))
                .andExpect(jsonPath("$[0].motorAction.id").value("ANKLE_DORSIFLEXION"))
                .andExpect(jsonPath("$[0].safetyNote").isNotEmpty());
    }
}
