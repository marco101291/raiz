"use client";

import { useCallback, useEffect, useState, useSyncExternalStore } from "react";
import { api, type Schemas } from "@/lib/api/client";
import {
  buildRequest,
  getDraft,
  painStaysAxial,
  resetDraft,
  setDraft,
  strengthApplies,
  subscribeDraft,
  type Draft,
} from "./draft";
import { MapStep } from "./MapStep";
import { PainStep } from "./PainStep";
import { Progress } from "./Progress";
import { historyNotes } from "./history";
import { ResultStep } from "./ResultStep";
import { consultationNotes, weaknessFrom } from "./strength";
import { StrengthStep } from "./StrengthStep";
import { TriageStep } from "./TriageStep";
import styles from "./Wizard.module.css";

type Catalog =
  | { status: "loading" }
  | { status: "error" }
  | {
      status: "ready";
      flags: Schemas["RedFlagView"][];
      painOptions: Schemas["PainOptionsView"];
      bodyZones: Schemas["BodyZoneView"][];
      strengthTests: Schemas["StrengthTestView"][];
      functionalQuestions: Schemas["FunctionalQuestionView"][];
    };

/** There is no sessionStorage on the server: the loading state is rendered and the client hydrates. */
const serverDraft = () => null;

/**
 * Orchestrates the steps. Each step only collects answers; this decides where to go next.
 * Flow: red flags → (none) pain → map → strength → result. With a red flag it jumps to the result; without
 * radiation or marked zones it skips strength (README, Forms).
 */
export function Wizard() {
  const draft = useSyncExternalStore(subscribeDraft, getDraft, serverDraft);
  const [catalog, setCatalog] = useState<Catalog>({ status: "loading" });

  const loadCatalog = useCallback(() => {
    // The form catalogs are fetched in parallel; if any fails, the person can retry.
    Promise.all([
      api.redFlags(),
      api.painOptions(),
      api.bodyZones(),
      api.strengthTests(),
      api.functionalQuestions(),
    ]).then(
      ([flags, painOptions, bodyZones, strengthTests, functionalQuestions]) =>
        setCatalog({ status: "ready", flags, painOptions, bodyZones, strengthTests, functionalQuestions }),
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

  // Leg tests for pain that starts in the low back, everyday-task questions for the neck.
  const regions = draft.pain?.regions ?? [];
  const tests = regions.includes("LUMBOSACRAL") ? catalog.strengthTests : [];
  const questions = regions.includes("CERVICAL") ? catalog.functionalQuestions : [];
  const withStrength = strengthApplies(draft);

  if (draft.step === "resultado") {
    const alarm = buildRequest(draft).redFlags?.length;
    return (
      <ResultStep
        request={buildRequest(draft)}
        warningSigns={catalog.flags.filter((flag) => flag.emergency)}
        notes={[
          ...historyNotes(draft.pain, catalog.painOptions.discLevels ?? []),
          ...(withStrength ? consultationNotes(tests, draft.strength ?? {}, questions, draft.functional ?? {}) : []),
        ]}
        onEdit={() => update({ step: alarm ? "alarma" : withStrength ? "fuerza" : "mapa" })}
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
          // With a red flag it is sent right away and jumps to the result (README, Forms).
          onSubmit={(redFlags) => update({ step: redFlags.length > 0 ? "resultado" : "dolor" })}
        />
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
        <MapStep
          zones={catalog.bodyZones}
          optional={painStaysAxial(draft.pain)}
          regions={draft.pain?.regions ?? []}
          defaultValue={draft.sensoryZones ?? []}
          onChange={(sensoryZones) => update({ sensoryZones })}
          onBack={() => update({ step: "dolor" })}
          onSubmit={(sensoryZones) =>
            strengthApplies({ pain: draft.pain, sensoryZones })
              ? update({ sensoryZones, step: "fuerza" })
              : update({ sensoryZones, weakness: [], step: "resultado" })
          }
        />
      )}

      {draft.step === "fuerza" && (
        <StrengthStep
          tests={tests}
          questions={questions}
          defaultValues={{ tests: draft.strength ?? {}, functional: draft.functional ?? {} }}
          onChange={({ tests: strength, functional }) => update({ strength, functional })}
          onBack={() => update({ step: "mapa" })}
          onSubmit={({ tests: strength, functional }) =>
            update({ strength, functional, weakness: weaknessFrom(tests, strength), step: "resultado" })
          }
        />
      )}
    </div>
  );
}
