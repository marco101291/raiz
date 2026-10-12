package com.raiz.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Una raíz nerviosa con su territorio clínico.
 *
 * @param code             código de la raíz, p. ej. "L5"
 * @param dermatome        zonas sensitivas que inerva
 * @param myotome          movimientos que dependen principalmente de ella
 * @param reflexes         reflejos asociados (puede estar vacío: L5, C8, T1)
 * @param typicalDiscLevels niveles discales que típicamente la comprimen, con el tipo de hernia
 * @param provocationTests pruebas de tensión o provocación útiles para el profesional
 * @param patientSummary   explicación en lenguaje claro
 * @param clinicalNotes    notas para fisios, neurólogos y traumatólogos
 * @param sourceReference  referencias bibliográficas que respaldan el contenido clínico
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
