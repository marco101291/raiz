package com.raiz.web;

import com.raiz.domain.assessment.CorrelationEngine;
import com.raiz.domain.assessment.SymptomReport;
import com.raiz.domain.model.Sided;
import com.raiz.persistence.OtherCauseCatalog;
import com.raiz.persistence.ResultTextCatalog;
import com.raiz.web.dto.AssessmentRequest;
import com.raiz.web.dto.AssessmentResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stateless assessment: nothing is stored (README, principle 6). The response carries no clinical fields
 * of the roots, only their code, so it does not depend on the audience.
 */
@RestController
@RequestMapping("/api/v1/assessments")
class AssessmentController {

    private final CorrelationEngine engine;
    private final OtherCauseCatalog otherCauses;
    private final ResultTextCatalog texts;

    AssessmentController(CorrelationEngine engine, OtherCauseCatalog otherCauses, ResultTextCatalog texts) {
        this.engine = engine;
        this.otherCauses = otherCauses;
        this.texts = texts;
    }

    @PostMapping
    AssessmentResponse assess(@Valid @RequestBody AssessmentRequest request) {
        SymptomReport report = AssessmentMapper.toReport(request);
        var zones = report.sensoryZones().stream().map(Sided::finding).toList();
        return AssessmentMapper.toResponse(engine.assess(report), otherCauses.forZones(zones), texts);
    }
}
