package com.raiz.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NerveRootApiIT extends ApiIntegrationTest {

    // --- Regla del README: con audience=PATIENT no salen notas clínicas ni pruebas de provocación ---

    @Test
    void patientListNeverExposesClinicalFields() throws Exception {
        mvc.perform(get("/api/v1/nerve-roots").param("audience", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(11)))
                .andExpect(jsonPath("$[*].clinicalNotes").doesNotExist())
                .andExpect(jsonPath("$[*].provocationTests").doesNotExist())
                .andExpect(jsonPath("$[*].typicalDiscLevels").doesNotExist());
    }

    @Test
    void withoutAudienceTheDetailIsThePatientView() throws Exception {
        mvc.perform(get("/api/v1/nerve-roots/L5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("L5"))
                .andExpect(jsonPath("$.patientSummary").isNotEmpty())
                .andExpect(jsonPath("$.clinicalNotes").doesNotExist())
                .andExpect(jsonPath("$.provocationTests").doesNotExist())
                .andExpect(jsonPath("$.typicalDiscLevels").doesNotExist());
    }

    @Test
    void professionalDetailIncludesClinicalFieldsInOrder() throws Exception {
        mvc.perform(get("/api/v1/nerve-roots/L5").param("audience", "PROFESSIONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clinicalNotes").isNotEmpty())
                .andExpect(jsonPath("$.provocationTests", contains("SLR", "slump", "SLR cruzado")))
                .andExpect(jsonPath("$.typicalDiscLevels", contains("L4-L5 posterolateral", "L5-S1 foraminal")))
                .andExpect(jsonPath("$.dermatome[?(@.id == 'GREAT_TOE')].label").value("Dedo gordo del pie"));
    }

    // --- Listado ---

    @Test
    void listFollowsAnatomicalOrder() throws Exception {
        mvc.perform(get("/api/v1/nerve-roots"))
                .andExpect(jsonPath("$[*].code",
                        contains("C5", "C6", "C7", "C8", "T1", "L1", "L2", "L3", "L4", "L5", "S1")));
    }

    @Test
    void filtersByRegion() throws Exception {
        mvc.perform(get("/api/v1/nerve-roots").param("region", "CERVICAL"))
                .andExpect(jsonPath("$[*].code", contains("C5", "C6", "C7", "C8", "T1")));
    }

    @Test
    void codeIsCaseInsensitive() throws Exception {
        mvc.perform(get("/api/v1/nerve-roots/s1")).andExpect(jsonPath("$.code").value("S1"));
    }

    // --- Errores ---

    @Test
    void unknownRootIs404ProblemDetail() throws Exception {
        mvc.perform(get("/api/v1/nerve-roots/X9"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Raíz no encontrada"))
                .andExpect(jsonPath("$.detail").value("No existe la raíz X9 en el catálogo."));
    }

    @Test
    void invalidAudienceIs400() throws Exception {
        mvc.perform(get("/api/v1/nerve-roots").param("audience", "DOCTOR"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Parámetro no válido"));
    }
}
