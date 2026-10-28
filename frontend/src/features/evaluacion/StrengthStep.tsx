"use client";

import { useState } from "react";
import type { Schemas } from "@/lib/api/client";
import { FUNCTIONAL_ANSWERS, TEST_ANSWERS } from "./strength";
import step from "./step.module.css";
import styles from "./StrengthStep.module.css";

export interface StrengthAnswers {
  /** Strength test id → answer, e.g. { HEEL_WALK: "WEAKER_RIGHT" }. */
  tests: Record<string, string>;
  /** Functional question id → answer, e.g. { HAND_GRIP: "LEFT" }. */
  functional: Record<string, string>;
}

interface Props {
  /** Leg tests, shown when the pain starts in the low back. Empty hides the section. */
  tests: Schemas["StrengthTestView"][];
  /** Everyday-task questions, shown when the pain starts in the neck. Empty hides the section. */
  questions: Schemas["FunctionalQuestionView"][];
  defaultValues: StrengthAnswers;
  onChange: (answers: StrengthAnswers) => void;
  onBack: () => void;
  onSubmit: (answers: StrengthAnswers) => void;
}

/**
 * Step 4. Every test and question needs an answer, but "No la hice" is always an option: nobody has to do a
 * test they do not feel safe doing. A single piece of state per group: no need for React Hook Form.
 */
export function StrengthStep({ tests, questions, defaultValues, onChange, onBack, onSubmit }: Props) {
  const [answers, setAnswers] = useState<StrengthAnswers>(defaultValues);
  const [submitted, setSubmitted] = useState(false);

  const missingTests = tests.filter((test) => !answers.tests[test.id]);
  const missingQuestions = questions.filter((question) => !answers.functional[question.id]);
  const missing = missingTests.length + missingQuestions.length;
  // When every test carries the same safety note, it is shown once above them instead of six times.
  const firstNote = tests[0]?.safetyNote;
  const sharedNote = tests.every((test) => test.safetyNote === firstNote) ? firstNote : undefined;

  const answer = (group: keyof StrengthAnswers, id: string, value: string) => {
    const next = { ...answers, [group]: { ...answers[group], [id]: value } };
    setAnswers(next);
    onChange(next);
  };

  return (
    <form
      className={step.form}
      noValidate
      onSubmit={(event) => {
        event.preventDefault();
        setSubmitted(true);
        if (missing === 0) {
          onSubmit(answers);
        }
      }}
    >
      <header className={step.encabezado}>
        <h1 className={step.titulo} tabIndex={-1}>¿Has perdido fuerza?</h1>
        <p className={step.intro}>
          {tests.length > 0
            ? "Haz cada prueba despacio y compara un lado con el otro. Si alguna no te parece segura, elige «No la hice»."
            : "Contesta pensando en las últimas semanas."}
        </p>
      </header>

      {submitted && missing > 0 && (
        <p role="alert" className={step.resumenError}>
          {missing === 1 ? "Te falta 1 respuesta." : `Te faltan ${missing} respuestas.`}
        </p>
      )}

      {tests.length > 0 && (
        <section className={styles.seccion} aria-labelledby="piernas">
          <h2 id="piernas">Tus piernas</h2>
          {sharedNote && <p className={styles.cuidado}>{sharedNote}</p>}
          {tests.map((test) => (
            <Question
              key={test.id}
              id={`test-${test.id}`}
              title={test.name}
              details={[test.instructions]}
              note={sharedNote ? undefined : test.safetyNote}
              options={TEST_ANSWERS}
              value={answers.tests[test.id]}
              invalid={submitted && missingTests.includes(test)}
              onChange={(value) => answer("tests", test.id, value)}
            />
          ))}
        </section>
      )}

      {questions.length > 0 && (
        <section className={styles.seccion} aria-labelledby="brazos">
          <h2 id="brazos">Tus brazos</h2>
          <p className={styles.cuidado}>
            Probar la fuerza de los brazos a solas no es fiable, así que te preguntamos por tareas de todos los días.
          </p>
          {questions.map((question) => (
            <Question
              key={question.id}
              id={`functional-${question.id}`}
              title={question.question}
              options={FUNCTIONAL_ANSWERS}
              value={answers.functional[question.id]}
              invalid={submitted && missingQuestions.includes(question)}
              onChange={(value) => answer("functional", question.id, value)}
            />
          ))}
        </section>
      )}

      <div className={step.acciones}>
        <button type="button" className={step.secundario} onClick={onBack}>
          Atrás
        </button>
        <button type="submit" className={step.principal}>
          Ver mi resultado
        </button>
      </div>
    </form>
  );
}

interface QuestionProps {
  id: string;
  title: string;
  details?: string[];
  note?: string;
  options: readonly { id: string; label: string }[];
  value?: string;
  invalid: boolean;
  onChange: (value: string) => void;
}

function Question({ id, title, details = [], note, options, value, invalid, onChange }: QuestionProps) {
  return (
    <fieldset
      className={styles.prueba}
      aria-invalid={invalid || undefined}
      aria-describedby={invalid ? `${id}-error` : undefined}
    >
      <legend className={styles.nombre}>{title}</legend>
      {details.map((detail) => (
        <p key={detail}>{detail}</p>
      ))}
      {note && <p className={styles.cuidado}>{note}</p>}
      <div className={styles.opciones}>
        {options.map((option) => (
          <label key={option.id} className={styles.opcion}>
            <input
              type="radio"
              name={id}
              value={option.id}
              checked={value === option.id}
              onChange={() => onChange(option.id)}
            />
            <span>{option.label}</span>
          </label>
        ))}
      </div>
      {invalid && (
        <p id={`${id}-error`} className={step.error}>
          Elige una respuesta.
        </p>
      )}
    </fieldset>
  );
}
