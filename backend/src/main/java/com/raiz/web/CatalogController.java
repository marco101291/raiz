package com.raiz.web;

import com.raiz.domain.model.Aggravator;
import com.raiz.domain.model.BodyZone;
import com.raiz.domain.model.NerveRoot;
import com.raiz.domain.model.PainDistribution;
import com.raiz.domain.model.PainQuality;
import com.raiz.domain.model.RedFlag;
import com.raiz.domain.model.Reliever;
import com.raiz.domain.port.NerveRootCatalog;
import com.raiz.persistence.StrengthTestCatalog;
import com.raiz.web.dto.CatalogViews.BodyZoneView;
import com.raiz.web.dto.CatalogViews.PainOptionsView;
import com.raiz.web.dto.CatalogViews.StrengthTestView;
import com.raiz.web.dto.CodeLabel;
import com.raiz.web.dto.RedFlagView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/** Opciones de los formularios con sus etiquetas, para que el frontend no las duplique. */
@RestController
@RequestMapping("/api/v1/catalog")
class CatalogController {

    private final NerveRootCatalog roots;
    private final StrengthTestCatalog strengthTests;

    CatalogController(NerveRootCatalog roots, StrengthTestCatalog strengthTests) {
        this.roots = roots;
        this.strengthTests = strengthTests;
    }

    @GetMapping("/body-zones")
    List<BodyZoneView> bodyZones() {
        return Arrays.stream(BodyZone.values())
                .map(z -> new BodyZoneView(z.name(), z.label(), CodeLabel.of(z.region(), z.region().label())))
                .toList();
    }

    @GetMapping("/red-flags")
    List<RedFlagView> redFlags() {
        return Arrays.stream(RedFlag.values()).map(RedFlagView::of).toList();
    }

    @GetMapping("/pain-options")
    PainOptionsView painOptions() {
        return new PainOptionsView(
                CodeLabel.sorted(List.of(PainQuality.values()), PainQuality::label),
                CodeLabel.sorted(List.of(Aggravator.values()), Aggravator::label),
                CodeLabel.sorted(List.of(Reliever.values()), Reliever::label),
                CodeLabel.sorted(List.of(PainDistribution.values()), PainDistribution::label));
    }

    @GetMapping("/strength-tests")
    List<StrengthTestView> strengthTests() {
        return strengthTests.findAll().stream()
                .map(t -> new StrengthTestView(t.id(), t.name(), t.instructions(), t.safetyNote(),
                        CodeLabel.of(t.motorAction(), t.motorAction().label()),
                        roots.findAll().stream()
                                .filter(root -> root.myotome().contains(t.motorAction()))
                                .map(NerveRoot::code)
                                .toList()))
                .toList();
    }
}
