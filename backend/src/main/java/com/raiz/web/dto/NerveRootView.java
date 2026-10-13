package com.raiz.web.dto;

import com.raiz.domain.model.BodyZone;
import com.raiz.domain.model.MotorAction;
import com.raiz.domain.model.NerveRoot;
import com.raiz.domain.model.Reflex;
import com.raiz.web.Audience;

import java.util.List;

/**
 * Ficha de una raíz. Hay un tipo por audiencia: la vista de paciente no tiene los campos
 * clínicos, así que no hay forma de que salgan por descuido.
 */
public sealed interface NerveRootView {

    static NerveRootView of(NerveRoot root, Audience audience) {
        return switch (audience) {
            case PATIENT -> PatientView.from(root);
            case PROFESSIONAL -> ProfessionalView.from(root);
        };
    }

    record PatientView(
            String code,
            CodeLabel region,
            List<CodeLabel> dermatome,
            List<CodeLabel> myotome,
            List<CodeLabel> reflexes,
            String patientSummary,
            String sourceReference) implements NerveRootView {

        static PatientView from(NerveRoot root) {
            return new PatientView(root.code(), CodeLabel.of(root.region(), root.region().label()),
                    CodeLabel.sorted(root.dermatome(), BodyZone::label),
                    CodeLabel.sorted(root.myotome(), MotorAction::label),
                    CodeLabel.sorted(root.reflexes(), Reflex::label),
                    root.patientSummary(), root.sourceReference());
        }
    }

    record ProfessionalView(
            String code,
            CodeLabel region,
            List<CodeLabel> dermatome,
            List<CodeLabel> myotome,
            List<CodeLabel> reflexes,
            List<String> typicalDiscLevels,
            List<String> provocationTests,
            String patientSummary,
            String clinicalNotes,
            String sourceReference) implements NerveRootView {

        static ProfessionalView from(NerveRoot root) {
            return new ProfessionalView(root.code(), CodeLabel.of(root.region(), root.region().label()),
                    CodeLabel.sorted(root.dermatome(), BodyZone::label),
                    CodeLabel.sorted(root.myotome(), MotorAction::label),
                    CodeLabel.sorted(root.reflexes(), Reflex::label),
                    root.typicalDiscLevels(), root.provocationTests(),
                    root.patientSummary(), root.clinicalNotes(), root.sourceReference());
        }
    }
}
