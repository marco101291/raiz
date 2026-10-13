"use client";

import { useCallback, useEffect, useState, useSyncExternalStore } from "react";
import { api, type Schemas } from "@/lib/api/client";
import { buildRequest, getDraft, resetDraft, setDraft, subscribeDraft, type Draft } from "./draft";
import { PainStep } from "./PainStep";
import { Progress } from "./Progress";
import { ResultStep } from "./ResultStep";
import { TriageStep } from "./TriageStep";
import styles from "./Wizard.module.css";

type Catalog =
  | { status: "loading" }
  | { status: "error" }
  | { status: "ready"; flags: Schemas["RedFlagView"][]; painOptions: Schemas["PainOptionsView"] };

/** En el servidor no hay sessionStorage: se renderiza el estado de carga y el cliente hidrata. */
const serverDraft = () => null;

/**
 * Orquesta los pasos. Cada paso solo junta respuestas; aquí se decide a cuál ir.
 * Flujo actual: alarma → (sin alarma) dolor → resultado. Con alarma se salta directo al resultado.
 * El mapa corporal y la fuerza guiada se insertarán entre dolor y resultado.
 */
export function Wizard() {
  const draft = useSyncExternalStore(subscribeDraft, getDraft, serverDraft);
  const [catalog, setCatalog] = useState<Catalog>({ status: "loading" });

  const loadCatalog = useCallback(() => {
    // Los catálogos del formulario se piden en paralelo; si falla cualquiera, se puede reintentar.
    Promise.all([api.redFlags(), api.painOptions()]).then(
      ([flags, painOptions]) => setCatalog({ status: "ready", flags, painOptions }),
      () => setCatalog({ status: "error" }),
    );
  }, []);

  useEffect(loadCatalog, [loadCatalog]);

  const update = (patch: Partial<Draft>) => setDraft({ ...getDraft(), ...patch });

  if (draft === null || catalog.status === "loading") {
    return <p className={styles.estado}>Cargando las preguntas…</p>;
  }

  if (catalog.status === "error") {
    return (
      <div className={styles.estado} role="alert">
        <p>No pudimos cargar las preguntas. Revisa tu conexión y vuelve a intentarlo.</p>
        <button
          type="button"
          className={styles.reintentar}
          onClick={() => {
            setCatalog({ status: "loading" });
            loadCatalog();
          }}
        >
          Reintentar
        </button>
      </div>
    );
  }

  if (draft.step === "resultado") {
    const alarm = buildRequest(draft).redFlags?.length;
    return (
      <ResultStep
        request={buildRequest(draft)}
        onEdit={() => update({ step: alarm ? "alarma" : "dolor" })}
        onRestart={resetDraft}
      />
    );
  }

  return (
    <div className={styles.paso}>
      <Progress current={draft.step} />

      {draft.step === "alarma" && (
        <TriageStep
          flags={catalog.flags}
          defaultValues={draft.triage}
          onChange={(triage) => update({ triage })}
          // Con alarma se envía en ese momento y se salta al resultado (README, Formularios).
          onSubmit={(redFlags) => update({ step: redFlags.length > 0 ? "resultado" : "dolor" })}
        />
      )}

      {draft.step === "dolor" && (
        <PainStep
          options={catalog.painOptions}
          defaultValues={draft.pain ?? {}}
          onChange={(pain) => update({ pain })}
          onBack={() => update({ step: "alarma" })}
          onSubmit={(pain) => update({ pain, step: "resultado" })}
        />
      )}
    </div>
  );
}
