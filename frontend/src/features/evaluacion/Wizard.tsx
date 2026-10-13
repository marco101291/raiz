"use client";

import Link from "next/link";
import { useCallback, useEffect, useState, useSyncExternalStore } from "react";
import { api, type AssessmentResponse, type Schemas } from "@/lib/api/client";
import { getDraft, setDraft, subscribeDraft, type Draft } from "./draft";
import { PainStep } from "./PainStep";
import { Progress } from "./Progress";
import { TriageStep } from "./TriageStep";
import { UrgentResult } from "./UrgentResult";
import styles from "./Wizard.module.css";

type Catalog =
  | { status: "loading" }
  | { status: "error" }
  | { status: "ready"; flags: Schemas["RedFlagView"][]; painOptions: Schemas["PainOptionsView"] };

/** En el servidor no hay sessionStorage: se renderiza el estado de carga y el cliente hidrata. */
const serverDraft = () => null;

export function Wizard() {
  const draft = useSyncExternalStore(subscribeDraft, getDraft, serverDraft);
  const [catalog, setCatalog] = useState<Catalog>({ status: "loading" });
  const [result, setResult] = useState<AssessmentResponse | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(false);

  const loadCatalog = useCallback(() => {
    // Los catálogos del formulario se piden en paralelo; si falla cualquiera, se puede reintentar.
    Promise.all([api.redFlags(), api.painOptions()]).then(
      ([flags, painOptions]) => setCatalog({ status: "ready", flags, painOptions }),
      () => setCatalog({ status: "error" }),
    );
  }, []);

  useEffect(loadCatalog, [loadCatalog]);

  const update = (patch: Partial<Draft>) => setDraft({ ...getDraft(), ...patch });

  const submitAlarm = async (redFlags: string[]) => {
    if (redFlags.length === 0) {
      update({ step: "dolor" });
      return;
    }
    // Con alarma se envía en este momento y se salta al resultado (README, Formularios).
    setSubmitting(true);
    setSubmitError(false);
    try {
      setResult(await api.assess({ redFlags: redFlags as Schemas["AssessmentRequest"]["redFlags"] }));
    } catch {
      setSubmitError(true);
    } finally {
      setSubmitting(false);
    }
  };

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

  if (result?.type === "URGENT_REFERRAL") {
    return <UrgentResult result={result} onEdit={() => setResult(null)} />;
  }

  return (
    <div className={styles.paso}>
      <Progress current={draft.step} />

      {draft.step === "alarma" && (
        <>
          <TriageStep
            flags={catalog.flags}
            defaultValues={draft.triage}
            onChange={(triage) => update({ triage })}
            onSubmit={submitAlarm}
            submitting={submitting}
          />
          {submitError && (
            <p role="alert" className={styles.errorEnvio}>
              No pudimos obtener la orientación. Si tienes alguna de estas señales, no esperes: acude a
              urgencias o llama al 911.
            </p>
          )}
        </>
      )}

      {draft.step === "dolor" && (
        <PainStep
          options={catalog.painOptions}
          defaultValues={draft.pain ?? {}}
          onChange={(pain) => update({ pain })}
          onBack={() => update({ step: "alarma" })}
          onSubmit={(pain) => update({ pain, step: "mapa" })}
        />
      )}

      {draft.step === "mapa" && (
        <div className={styles.pendiente}>
          <h1>Mapa del cuerpo</h1>
          <p>Este paso todavía está en construcción.</p>
          <button type="button" className={styles.reintentar} onClick={() => update({ step: "dolor" })}>
            Volver a tu dolor
          </button>
          <Link href="/">Volver al inicio</Link>
        </div>
      )}
    </div>
  );
}
