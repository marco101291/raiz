package com.raiz.web.dto;

import com.raiz.domain.model.Aggravator;
import com.raiz.domain.assessment.MedicalHistory.PriorHerniation;
import com.raiz.domain.model.BodyZone;
import com.raiz.domain.model.DiscLevel;
import com.raiz.domain.model.MotorAction;
import com.raiz.domain.model.PainDistribution;
import com.raiz.domain.model.PainQuality;
import com.raiz.domain.model.RedFlag;
import com.raiz.domain.model.Reflex;
import com.raiz.domain.model.Reliever;
import com.raiz.domain.model.Side;
import com.raiz.domain.model.SpinalRegion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Body of {@code POST /api/v1/assessments}. It validates the shape (types, ranges, nulls); the domain
 * applies the clinical rules. Lists may be missing: that means empty.
 */
public record AssessmentRequest(
        Set<@NotNull(message = "no puede contener valores nulos") RedFlag> redFlags,
        @Valid Pain pain,
        Set<@NotNull(message = "no puede contener valores nulos") @Valid SidedFinding<BodyZone>> sensoryZones,
        Set<@NotNull(message = "no puede contener valores nulos") @Valid SidedFinding<MotorAction>> weakness,
        Set<@NotNull(message = "no puede contener valores nulos") @Valid SidedFinding<Reflex>> diminishedReflexes,
        @Valid History history) {

    public record Pain(
            @NotNull(message = "es obligatoria")
            @Min(value = 0, message = "debe estar entre 0 y 10")
            @Max(value = 10, message = "debe estar entre 0 y 10")
            Integer intensity,
            @NotNull(message = "es obligatoria")
            @Min(value = 0, message = "no puede ser negativa")
            Integer durationDays,
            @NotEmpty(message = "indica dónde empieza el dolor")
            List<@NotNull(message = "no puede contener valores nulos") @Valid Reach> reach,
            Set<@NotNull(message = "no puede contener valores nulos") PainQuality> qualities,
            Set<@NotNull(message = "no puede contener valores nulos") Aggravator> aggravators,
            Set<@NotNull(message = "no puede contener valores nulos") Reliever> relievers) {
    }

    /**
     * Where the pain starts and how far it reaches from there, e.g.
     * {@code {"region": "LUMBOSACRAL", "distribution": "PROXIMAL"}}. One entry per region; both if the pain
     * starts in the neck and in the low back.
     */
    public record Reach(
            @NotNull(message = "es obligatoria") SpinalRegion region,
            @NotNull(message = "es obligatoria") PainDistribution distribution) {
    }

    /**
     * Prior herniation and spine surgery. Optional as a whole; when sent, the herniation answer is required.
     *
     * @param herniationLevels  only when {@code priorHerniation} is YES, and only if the person knows them
     * @param priorSpineSurgery missing means no: a boxed Boolean, since Jackson rejects a missing primitive
     */
    public record History(
            @NotNull(message = "es obligatoria") PriorHerniation priorHerniation,
            Set<@NotNull(message = "no puede contener valores nulos") DiscLevel> herniationLevels,
            Boolean priorSpineSurgery) {
    }

    /** A finding with its side, e.g. {@code {"finding": "GREAT_TOE", "side": "RIGHT"}}. */
    public record SidedFinding<T>(
            @NotNull(message = "es obligatorio") T finding,
            @NotNull(message = "es obligatorio") Side side) {
    }
}
