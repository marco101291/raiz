package com.raiz.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A nerve root with its clinical territory.
 *
 * @param code             root code, e.g. "L5"
 * @param dermatome        sensory zones it supplies
 * @param myotome          movements that depend mainly on it
 * @param reflexes         associated reflexes (may be empty: L5, C8, T1)
 * @param typicalDiscLevels disc levels that typically compress it, with the herniation type
 * @param provocationTests tension or provocation tests useful to the professional
 * @param patientSummary   plain-language explanation
 * @param clinicalNotes    notes for physiotherapists, neurologists and orthopedists
 * @param sourceReference  bibliographic references backing the clinical content
 */
public record NerveRoot(
        String code,
        SpinalRegion region,
        Set<BodyZone> dermatome,
        Set<MotorAction> myotome,
        Set<Reflex> reflexes,
        List<String> typicalDiscLevels,
        List<String> provocationTests,
        String patientSummary,
        String clinicalNotes,
        String sourceReference) {

    public NerveRoot {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(region, "region");
        dermatome = Set.copyOf(dermatome);
        myotome = Set.copyOf(myotome);
        reflexes = Set.copyOf(reflexes);
        typicalDiscLevels = List.copyOf(typicalDiscLevels);
        provocationTests = List.copyOf(provocationTests);
    }
}
