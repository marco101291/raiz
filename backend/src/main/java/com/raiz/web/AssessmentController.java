package com.raiz.web;

import com.raiz.domain.assessment.CorrelationEngine;
import com.raiz.web.dto.AssessmentRequest;
import com.raiz.web.dto.AssessmentResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Evaluación sin estado: no se guarda nada (README, principio 6). La respuesta no lleva campos
 * clínicos de las raíces, solo su código, así que no depende de la audiencia.
 */
@RestController
@RequestMapping("/api/v1/assessments")
class AssessmentController {

    private final CorrelationEngine engine;

    AssessmentController(CorrelationEngine engine) {
        this.engine = engine;
    }

    @PostMapping
    AssessmentResponse assess(@Valid @RequestBody AssessmentRequest request) {
        return AssessmentMapper.toResponse(engine.assess(AssessmentMapper.toReport(request)));
    }
}
