"use client";

import { useEffect, useState } from "react";
import { api, type AssessmentRequest, type AssessmentResponse } from "@/lib/api/client";
import { CorrelationResult } from "./CorrelationResult";
import result from "./result.module.css";
import step from "./step.module.css";
import { UrgentResult } from "./UrgentResult";

type State = { status: "loading" } | { status: "error" } | { status: "ready"; assessment: AssessmentResponse };

interface Props {
  request: AssessmentRequest;
  onEdit: () => void;
  onRestart: () => void;
}

/**
 * Paso 5. Se pide al motor al llegar aquí: una sola petición por evaluación, y si la persona recarga
 * se vuelve a pedir (Raíz no guarda evaluaciones). Orden fijo: derivación, patrón, raíces, aviso.
 */
export function ResultStep({ request, onEdit, onRestart }: Props) {
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
        <CorrelationResult result={assessment} />
      )}
      <p className={result.aviso}>{assessment.referral?.disclaimer}</p>
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
