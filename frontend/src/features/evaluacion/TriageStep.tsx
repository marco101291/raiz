"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect, useEffectEvent, useMemo } from "react";
import { useForm, useWatch } from "react-hook-form";
import { z } from "zod";
import type { Schemas } from "@/lib/api/client";
import { reportedRedFlags, type Answer } from "./draft";
import step from "./step.module.css";
import styles from "./TriageStep.module.css";

type RedFlag = Schemas["RedFlagView"];
/** While filling in: an unanswered question is null (React Hook Form radios) or undefined. */
type Answers = Record<string, Answer | null | undefined>;

interface Props {
  flags: RedFlag[];
  defaultValues: Record<string, Answer>;
  onChange: (answers: Record<string, Answer>) => void;
  /** Receives the red flags answered "sí" (empty if none). */
  onSubmit: (redFlags: string[]) => void;
}

/** Step 1. Every question is required: the person cannot continue without answering each one. */
export function TriageStep({ flags, defaultValues, onChange, onSubmit }: Props) {
  // The schema depends on the catalog from the backend: one question per red flag.
  const schema = useMemo(
    () =>
      z.object(
        Object.fromEntries(
          // Input: may be missing or null (unanswered). Validated output: always "si" or "no".
          flags.map((flag) => [
            flag.id,
            z.enum(["si", "no"]).nullish().pipe(z.enum(["si", "no"], { error: "Elige sí o no." })),
          ]),
        ),
      ),
    [flags],
  );

  const {
    register,
    handleSubmit,
    control,
    subscribe,
    formState: { errors, isSubmitted },
  } = useForm<Answers, unknown, Record<string, Answer>>({ resolver: zodResolver(schema), defaultValues, shouldFocusError: true });

  // subscribe delivers the already updated values; the <form> onChange event arrives before React Hook
  // Form and would save the previous answer. useEffectEvent avoids resubscribing on every render.
  const notifyChange = useEffectEvent((values: Answers) => onChange(onlyAnswered(values)));
  useEffect(
    () => subscribe({ formState: { values: true }, callback: ({ values }) => notifyChange(values) }),
    [subscribe],
  );

  const answers = useWatch({ control });
  const anyYes = reportedRedFlags(onlyAnswered(answers)).length > 0;
  const missing = flags.filter((flag) => errors[flag.id]).length;

  return (
    <form
      className={step.form}
      noValidate
      onSubmit={handleSubmit((values) => onSubmit(reportedRedFlags(values)))}
    >
      <header className={step.encabezado}>
        <h1 className={step.titulo}>Antes de empezar, ¿te pasa alguna de estas cosas?</h1>
        <p className={step.intro}>
          Algunas señales necesitan atención médica sin esperar. Responde cada una; si alguna es sí, te
          decimos qué hacer.
        </p>
      </header>

      {isSubmitted && missing > 0 && (
        <p role="alert" className={step.resumenError}>
          {missing === 1 ? "Te falta 1 pregunta por responder." : `Te faltan ${missing} preguntas por responder.`}
        </p>
      )}

      <div className={styles.preguntas}>
        {flags.map((flag) => {
          const id = flag.id;
          const error = errors[id]?.message;
          return (
            <fieldset key={id} className={styles.pregunta} aria-invalid={error ? true : undefined}>
              <legend className={styles.enunciado}>{flag.label}</legend>
              <div className={styles.opciones}>
                {(["si", "no"] as const).map((value) => (
                  <label key={value} className={styles.opcion}>
                    <input type="radio" value={value} {...register(id)} aria-describedby={error ? `${id}-error` : undefined} />
                    <span>{value === "si" ? "Sí" : "No"}</span>
                  </label>
                ))}
              </div>
              {error && (
                <p id={`${id}-error`} className={`${step.error} ${styles.errorPregunta}`}>
                  {error}
                </p>
              )}
            </fieldset>
          );
        })}
      </div>

      <button type="submit" className={step.principal}>
        {anyYes ? "Ver qué hacer" : "Continuar"}
      </button>
    </form>
  );
}

function onlyAnswered(answers: Answers): Record<string, Answer> {
  return Object.fromEntries(
    Object.entries(answers).filter((entry): entry is [string, Answer] => entry[1] != null),
  );
}
