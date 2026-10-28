"use client";

import { useEffect, useState } from "react";
import { api, type AssessmentRequest, type AssessmentResponse, type Schemas } from "@/lib/api/client";
import { CorrelationResult } from "./CorrelationResult";
import result from "./result.module.css";
import step from "./step.module.css";
import { UrgentResult } from "./UrgentResult";

type State = { status: "loading" } | { status: "error" } | { status: "ready"; assessment: AssessmentResponse };

interface Props {
  request: AssessmentRequest;
  /** Emergency red flags from the triage catalog, shown on every non-urgent result. */
  warningSigns?: Schemas["RedFlagView"][];
  /** History and strength answers worth telling a professional: not part of the engine's decision. */
  notes?: string[];
  onEdit: () => void;
  onRestart: () => void;
}

/**
 * Step 5. The engine is called on arrival: one request per assessment, repeated if the person reloads
 * (Raíz does not store assessments). Fixed order: referral, when to go to the ER, what it may be, roots
 * for the doctor, disclaimer.
 */
export function ResultStep({ request, warningSigns = [], notes = [], onEdit, onRestart }: Props) {
  const [state, setState] = useState<State>({ status: "loading" });
  const [attempt, setAttempt] = useState(0);
  const body = JSON.stringify(request);

  useEffect(() => {
    let active = true;
    api.assess(JSON.parse(body)).then(
      (assessment) => active && setState({ status: "ready", assessment }),
      () => active && setState({ status: "error" }),
    );
    return () => {
      active = false;
    };
  }, [body, attempt]);

  if (state.status === "loading") {
    return <p className={result.ayuda}>Preparando tu orientación…</p>;
  }

  if (state.status === "error") {
    const alarm = (request.redFlags?.length ?? 0) > 0;
    return (
      <div role="alert" className={result.resultado}>
        <p className={step.resumenError}>
          {alarm
            ? "No pudimos obtener la orientación. Si tienes alguna de las señales que marcaste, no esperes: acude a urgencias o llama al 911."
            : "No pudimos obtener la orientación. Revisa tu conexión y vuelve a intentarlo."}
        </p>
        <div className={step.acciones}>
          <button
            type="button"
            className={step.principal}
            onClick={() => {
              setState({ status: "loading" });
              setAttempt((n) => n + 1);
            }}
          >
            Reintentar
          </button>
          <button type="button" className={step.secundario} onClick={onEdit}>
            Cambiar mis respuestas
          </button>
        </div>
      </div>
    );
  }

  const { assessment } = state;
  return (
    <article className={result.resultado}>
      {assessment.type === "URGENT_REFERRAL" ? (
        <UrgentResult result={assessment} />
      ) : (
        <CorrelationResult result={assessment} warningSigns={warningSigns} notes={notes} />
      )}
      <p className={result.aviso}>{assessment.referral.disclaimer}</p>
      <div className={step.acciones}>
        <button type="button" className={step.secundario} onClick={onEdit}>
          Cambiar mis respuestas
        </button>
        <button type="button" className={step.secundario} onClick={onRestart}>
          Empezar de nuevo
        </button>
      </div>
    </article>
  );
}
