package com.raiz.web;

import com.raiz.domain.model.SpinalRegion;
import com.raiz.domain.port.NerveRootCatalog;
import com.raiz.web.dto.NerveRootView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/nerve-roots")
class NerveRootController {

    private final NerveRootCatalog catalog;

    NerveRootController(NerveRootCatalog catalog) {
        this.catalog = catalog;
    }

    /** Without {@code audience} the response is for a patient: the safe default. */
    @GetMapping
    List<NerveRootView> list(@RequestParam(required = false) SpinalRegion region,
                             @RequestParam(defaultValue = "PATIENT") Audience audience) {
        return catalog.findAll().stream()
                .filter(root -> region == null || root.region() == region)
                .map(root -> NerveRootView.of(root, audience))
                .toList();
    }

    @GetMapping("/{code}")
    NerveRootView get(@PathVariable String code, @RequestParam(defaultValue = "PATIENT") Audience audience) {
        return catalog.findByCode(code.toUpperCase(Locale.ROOT))
                .map(root -> NerveRootView.of(root, audience))
                .orElseThrow(() -> new NerveRootNotFoundException(code));
    }
}
