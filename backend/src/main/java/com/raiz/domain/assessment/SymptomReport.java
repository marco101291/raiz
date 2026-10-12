package com.raiz.domain.assessment;

import com.raiz.domain.model.*;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Lo que la persona (o el profesional) reporta en los formularios. Zonas, debilidad y reflejos
 * llevan siempre su lado.
 *
 * @param pain               cuestionario de dolor; puede faltar en modo profesional
 * @param diminishedReflexes normalmente los aporta un profesional; en modo paciente llegan vacíos
 */
public record SymptomReport(
        Set<RedFlag> redFlags,
        PainProfile pain,
        Set<Sided<BodyZone>> sensoryZones,
        Set<Sided<MotorAction>> weakness,
        Set<Sided<Reflex>> diminishedReflexes) {

    public SymptomReport {
        redFlags = redFlags == null ? Set.of() : Set.copyOf(redFlags);
        sensoryZones = sensoryZones == null ? Set.of() : Set.copyOf(sensoryZones);
        weakness = weakness == null ? Set.of() : Set.copyOf(weakness);
        diminishedReflexes = diminishedReflexes == null ? Set.of() : Set.copyOf(diminishedReflexes);
    }

    public Optional<PainProfile> painProfile() {
        return Optional.ofNullable(pain);
    }

    public boolean hasNeurologicalFindings() {
        return !sensoryZones.isEmpty() || !weakness.isEmpty() || !diminishedReflexes.isEmpty();
    }

    /**
     * Regiones (cervical o lumbosacra) con hallazgos en ambos lados. Síntomas en las dos piernas
     * o los dos brazos son un dato de alarma aunque el triage no lo haya marcado.
     */
    public Set<SpinalRegion> bilateralRegions() {
        Set<SpinalRegion> result = EnumSet.noneOf(SpinalRegion.class);
        for (SpinalRegion region : SpinalRegion.values()) {
            Set<Side> sides = findingsIn(region).map(RegionSide::side).collect(
                    () -> EnumSet.noneOf(Side.class), Set::add, Set::addAll);
            if (sides.size() == Side.values().length) {
                result.add(region);
            }
        }
        return result;
    }

    private Stream<RegionSide> findingsIn(SpinalRegion region) {
        return Stream.of(
                        sensoryZones.stream().map(s -> new RegionSide(s.finding().region(), s.side())),
                        weakness.stream().map(s -> new RegionSide(s.finding().region(), s.side())),
                        diminishedReflexes.stream().map(s -> new RegionSide(s.finding().region(), s.side())))
                .flatMap(s -> s)
                .filter(rs -> rs.region() == region);
    }

    private record RegionSide(SpinalRegion region, Side side) {
    }
}
