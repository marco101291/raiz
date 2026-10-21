package com.raiz.domain.assessment;

import com.raiz.domain.model.*;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * What the person (or the professional) reports in the forms. Zones, weakness and reflexes always
 * carry their side.
 *
 * @param pain               pain questionnaire; may be missing in professional mode
 * @param diminishedReflexes usually provided by a professional; empty in patient mode
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
     * Regions (cervical or lumbosacral) with findings on both sides. Symptoms in both legs or both arms
     * are a red flag even if the triage did not mark it.
     */
    public Set<SpinalRegion> bilateralRegions() {
        return bilateralRegions(zone -> true);
    }

    /**
     * Like {@link #bilateralRegions()}, but only the sensory zones that satisfy {@code countsZone} count.
     * Weakness and reflexes always count: they are loss of function, not just symptoms.
     */
    public Set<SpinalRegion> bilateralRegions(Predicate<BodyZone> countsZone) {
        Set<SpinalRegion> result = EnumSet.noneOf(SpinalRegion.class);
        for (SpinalRegion region : SpinalRegion.values()) {
            Set<Side> sides = findingsIn(region, countsZone).map(RegionSide::side).collect(
                    () -> EnumSet.noneOf(Side.class), Set::add, Set::addAll);
            if (sides.size() == Side.values().length) {
                result.add(region);
            }
        }
        return result;
    }

    private Stream<RegionSide> findingsIn(SpinalRegion region, Predicate<BodyZone> countsZone) {
        return Stream.of(
                        sensoryZones.stream().filter(s -> countsZone.test(s.finding()))
                                .map(s -> new RegionSide(s.finding().region(), s.side())),
                        weakness.stream().map(s -> new RegionSide(s.finding().region(), s.side())),
                        diminishedReflexes.stream().map(s -> new RegionSide(s.finding().region(), s.side())))
                .flatMap(s -> s)
                .filter(rs -> rs.region() == region);
    }

    private record RegionSide(SpinalRegion region, Side side) {
    }
}
