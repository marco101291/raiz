package com.raiz.domain;

import com.raiz.domain.assessment.*;
import com.raiz.domain.model.*;
import com.raiz.domain.port.NerveRootCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class CorrelationEngineTest {

    private static final NerveRoot L4 = root("L4",
            Set.of(BodyZone.MEDIAL_LEG, BodyZone.MEDIAL_MALLEOLUS),
            Set.of(MotorAction.ANKLE_DORSIFLEXION, MotorAction.KNEE_EXTENSION),
            Set.of(Reflex.PATELLAR));
    private static final NerveRoot L5 = root("L5",
            Set.of(BodyZone.LATERAL_LEG, BodyZone.DORSUM_FOOT, BodyZone.GREAT_TOE),
            Set.of(MotorAction.GREAT_TOE_EXTENSION, MotorAction.HIP_ABDUCTION, MotorAction.ANKLE_DORSIFLEXION),
            Set.of());
    private static final NerveRoot S1 = root("S1",
            Set.of(BodyZone.LATERAL_FOOT, BodyZone.SOLE, BodyZone.POSTERIOR_CALF),
            Set.of(MotorAction.ANKLE_PLANTAR_FLEXION, MotorAction.FOOT_EVERSION),
            Set.of(Reflex.ACHILLES));

    private final CorrelationEngine engine = new CorrelationEngine(new FixedCatalog(List.of(L4, L5, S1)));

    // --- Triage ---

    @Test
    void emergencyRedFlagShortCircuitsEverything() {
        var report = new SymptomReport(Set.of(RedFlag.SADDLE_ANESTHESIA), radicularPain(4, 3),
                Set.of(Sided.right(BodyZone.SOLE)), null, null);

        Assessment result = engine.assess(report);

        assertThat(result).isInstanceOf(Assessment.UrgentReferral.class);
        assertThat(result.referral().urgency()).isEqualTo(Urgency.EMERGENCY_NOW);
        assertThat(result.pattern().type()).isEqualTo(PatternInsight.Type.NOT_ASSESSED);
    }

    @Test
    void nonEmergencyRedFlagIsPromptNotEmergency() {
        Assessment result = engine.assess(new SymptomReport(Set.of(RedFlag.FEVER), null, null, null, null));

        assertThat(result.referral().urgency()).isEqualTo(Urgency.PROMPT);
    }

    // --- Correlación ---

    @Test
    void greatToeAndDorsumFootPointToL5() {
        var report = new SymptomReport(null, radicularPain(4, 5),
                Set.of(Sided.right(BodyZone.GREAT_TOE), Sided.right(BodyZone.DORSUM_FOOT)), null, null);

        assertThat(engine.assess(report)).asInstanceOf(type(Assessment.RootCorrelation.class))
                .satisfies(c -> assertThat(c.matches().getFirst().label()).isEqualTo("L5 derecha"))
                .satisfies(c -> assertThat(c.pattern().type()).isEqualTo(PatternInsight.Type.RADICULAR))
                .satisfies(c -> assertThat(c.referral().urgency()).isEqualTo(Urgency.ROUTINE));
    }

    @Test
    void weaknessEscalatesReferralToPrompt() {
        var report = new SymptomReport(null, null, Set.of(Sided.left(BodyZone.SOLE)),
                Set.of(Sided.left(MotorAction.ANKLE_PLANTAR_FLEXION)), Set.of(Sided.left(Reflex.ACHILLES)));

        assertThat(engine.assess(report)).asInstanceOf(type(Assessment.RootCorrelation.class))
                .satisfies(c -> assertThat(c.matches().getFirst().root().code()).isEqualTo("S1"))
                .satisfies(c -> assertThat(c.matches().getFirst().score()).isEqualTo(8))
                .satisfies(c -> assertThat(c.referral().urgency()).isEqualTo(Urgency.PROMPT));
    }

    @Test
    void sharedMotorActionRanksBothRootsInAnatomicalOrder() {
        var report = new SymptomReport(null, null, null, Set.of(Sided.right(MotorAction.ANKLE_DORSIFLEXION)), null);

        var correlation = (Assessment.RootCorrelation) engine.assess(report);

        assertThat(correlation.matches()).extracting(m -> m.root().code()).containsExactly("L4", "L5");
    }

    @Test
    void severeRadicularPainWithoutDeficitIsPrompt() {
        var report = new SymptomReport(null, radicularPain(9, 4), Set.of(Sided.right(BodyZone.LATERAL_LEG)), null, null);

        assertThat(engine.assess(report).referral().urgency()).isEqualTo(Urgency.PROMPT);
    }

    @Test
    void chronicRadicularSymptomsReferToSpineSpecialistFirst() {
        var report = new SymptomReport(null, radicularPain(4, 120), Set.of(Sided.right(BodyZone.LATERAL_LEG)), null, null);

        assertThat(engine.assess(report).referral().specialists().getFirst()).isEqualTo(Specialist.SPINE_SURGEON);
    }

    // --- Lateralidad ---

    @Test
    void findingsOnBothLegsInferBilateralRedFlag() {
        var report = new SymptomReport(null, radicularPain(4, 5),
                Set.of(Sided.right(BodyZone.LATERAL_LEG), Sided.left(BodyZone.SOLE)), null, null);

        assertThat(engine.assess(report)).asInstanceOf(type(Assessment.UrgentReferral.class))
                .satisfies(u -> assertThat(u.reported()).isEmpty())
                .satisfies(u -> assertThat(u.inferred()).containsExactly(RedFlag.BILATERAL_SYMPTOMS))
                .satisfies(u -> assertThat(u.referral().urgency()).isEqualTo(Urgency.EMERGENCY_NOW));
    }

    @Test
    void oneArmAndTheOtherLegIsNotBilateral() {
        var report = new SymptomReport(null, null,
                Set.of(Sided.left(BodyZone.THUMB), Sided.right(BodyZone.LATERAL_LEG)), null, null);

        assertThat(engine.assess(report)).isInstanceOf(Assessment.RootCorrelation.class);
    }

    @Test
    void evidenceIsNotMixedAcrossSides() {
        var report = new SymptomReport(null, null, Set.of(Sided.right(BodyZone.SOLE)),
                Set.of(Sided.right(MotorAction.ANKLE_PLANTAR_FLEXION)), null);

        var correlation = (Assessment.RootCorrelation) engine.assess(report);

        assertThat(correlation.matches()).singleElement()
                .satisfies(m -> assertThat(m.label()).isEqualTo("S1 derecha"))
                .satisfies(m -> assertThat(m.score()).isEqualTo(5));
    }

    // --- Patrón de dolor ---

    @Test
    void radicularPainWithoutMapSelectionAsksToUseTheMap() {
        var report = new SymptomReport(null, radicularPain(5, 7), null, null, null);

        assertThat(engine.assess(report)).asInstanceOf(type(Assessment.NoRootCorrelation.class))
                .satisfies(n -> assertThat(n.pattern().type()).isEqualTo(PatternInsight.Type.RADICULAR))
                .satisfies(n -> assertThat(n.note()).contains("mapa"));
    }

    @Test
    void flexionSittingAndCoughPatternIsDiscogenic() {
        var pain = new PainProfile(4, 5, PainDistribution.AXIAL_ONLY, Set.of(PainQuality.DULL_ACHE),
                Set.of(Aggravator.SITTING, Aggravator.FORWARD_BENDING, Aggravator.COUGH_SNEEZE),
                Set.of(Reliever.WALKING));

        Assessment result = engine.assess(new SymptomReport(null, pain, null, null, null));

        assertThat(result.pattern().type()).isEqualTo(PatternInsight.Type.DISCOGENIC);
        assertThat(result.pattern().features()).hasSize(4);
    }

    @Test
    void extensionPatternRelievedBySitting() {
        var pain = new PainProfile(3, 10, PainDistribution.AXIAL_ONLY, Set.of(PainQuality.STIFFNESS),
                Set.of(Aggravator.BACKWARD_BENDING, Aggravator.STANDING_LONG), Set.of(Reliever.SITTING));

        Assessment result = engine.assess(new SymptomReport(null, pain, null, null, null));

        assertThat(result.pattern().type()).isEqualTo(PatternInsight.Type.EXTENSION_SENSITIVE);
    }

    @Test
    void noFindingsStillRefers() {
        Assessment result = engine.assess(new SymptomReport(null, null, null, null, null));

        assertThat(result).isInstanceOf(Assessment.NoRootCorrelation.class);
        assertThat(result.referral().specialists()).isNotEmpty();
    }

    @Test
    void painProfileRejectsOutOfRangeIntensity() {
        org.assertj.core.api.Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> new PainProfile(11, 1, PainDistribution.AXIAL_ONLY, null, null, null));
    }

    private static PainProfile radicularPain(int intensity, int days) {
        return new PainProfile(intensity, days, PainDistribution.DISTAL,
                Set.of(PainQuality.BURNING, PainQuality.TINGLING),
                Set.of(Aggravator.SITTING), Set.of(Reliever.LYING_DOWN));
    }

    private static NerveRoot root(String code, Set<BodyZone> zones, Set<MotorAction> motor, Set<Reflex> reflexes) {
        return new NerveRoot(code, SpinalRegion.LUMBOSACRAL, zones, motor, reflexes,
                List.of(), List.of(), "", "");
    }

    private record FixedCatalog(List<NerveRoot> roots) implements NerveRootCatalog {
        public List<NerveRoot> findAll() {
            return roots;
        }

        public Optional<NerveRoot> findByCode(String code) {
            return roots.stream().filter(r -> r.code().equals(code)).findFirst();
        }
    }
}
