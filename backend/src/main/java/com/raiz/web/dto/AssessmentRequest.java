package com.raiz.web.dto;

import com.raiz.domain.model.Aggravator;
import com.raiz.domain.model.BodyZone;
import com.raiz.domain.model.MotorAction;
import com.raiz.domain.model.PainDistribution;
import com.raiz.domain.model.PainQuality;
import com.raiz.domain.model.RedFlag;
import com.raiz.domain.model.Reflex;
import com.raiz.domain.model.Reliever;
import com.raiz.domain.model.Side;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * Cuerpo de {@code POST /api/v1/assessments}. Valida la forma (tipos, rangos, nulos); las reglas
 * clínicas las aplica el dominio. Las listas pueden faltar: equivalen a vacías.
 */
public record AssessmentRequest(
        Set<@NotNull(message = "no puede contener valores nulos") RedFlag> redFlags,
        @Valid Pain pain,
        Set<@NotNull(message = "no puede contener valores nulos") @Valid SidedFinding<BodyZone>> sensoryZones,
        Set<@NotNull(message = "no puede contener valores nulos") @Valid SidedFinding<MotorAction>> weakness,
        Set<@NotNull(message = "no puede contener valores nulos") @Valid SidedFinding<Reflex>> diminishedReflexes) {

    public record Pain(
            @NotNull(message = "es obligatoria")
            @Min(value = 0, message = "debe estar entre 0 y 10")
            @Max(value = 10, message = "debe estar entre 0 y 10")
            Integer intensity,
            @NotNull(message = "es obligatoria")
            @Min(value = 0, message = "no puede ser negativa")
            Integer durationDays,
            @NotNull(message = "es obligatoria")
            PainDistribution distribution,
            Set<@NotNull(message = "no puede contener valores nulos") PainQuality> qualities,
            Set<@NotNull(message = "no puede contener valores nulos") Aggravator> aggravators,
            Set<@NotNull(message = "no puede contener valores nulos") Reliever> relievers) {
    }

    /** Un hallazgo con su lado, p. ej. {@code {"finding": "GREAT_TOE", "side": "RIGHT"}}. */
    public record SidedFinding<T>(
            @NotNull(message = "es obligatorio") T finding,
            @NotNull(message = "es obligatorio") Side side) {
    }
}
