"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect, useEffectEvent } from "react";
import { Controller, useForm, useWatch } from "react-hook-form";
import { z } from "zod";
import type { Schemas } from "@/lib/api/client";
import { ChipGroup } from "./ChipGroup";
import { daysBetween, describeDuration, isIsoDate, todayIso } from "./dates";
import { DistributionFigure } from "./DistributionFigure";
import type { PainDraft } from "./draft";
import { PainScale } from "./PainScale";
import step from "./step.module.css";
import styles from "./PainStep.module.css";

const schema = z.object({
  intensity: z.number().nullish().pipe(z.number({ error: "Elige un número del 0 al 10." }).int().min(0).max(10)),
  onsetDate: z
    .string()
    .min(1, "Elige la fecha.")
    .refine(isIsoDate, "Escribe una fecha válida.")
    .refine((date) => date <= todayIso(), "La fecha no puede ser posterior a hoy."),
  distribution: z.string().nullish().pipe(z.string({ error: "Elige hasta dónde llega el dolor." })),
  qualities: z.array(z.string()).min(1, "Elige al menos una."),
  aggravators: z.array(z.string()),
  relievers: z.array(z.string()),
});
/** Valores mientras se llena (puede faltar intensidad o distribución) y ya validados. */
type PainForm = z.input<typeof schema>;
export type PainAnswers = z.output<typeof schema>;

interface Props {
  options: Schemas["PainOptionsView"];
  defaultValues: PainDraft;
  onChange: (pain: PainDraft) => void;
  onBack: () => void;
  onSubmit: (pain: PainAnswers) => void;
}

/** Paso 2. Cuestionario de dolor (README, Formularios: PainProfile). */
export function PainStep({ options, defaultValues, onChange, onBack, onSubmit }: Props) {
  const {
    register,
    control,
    handleSubmit,
    subscribe,
    formState: { errors, isSubmitted },
  } = useForm<PainForm, unknown, PainAnswers>({
    resolver: zodResolver(schema),
    shouldFocusError: true,
    defaultValues: {
      intensity: null,
      onsetDate: "",
      distribution: null,
      qualities: [],
      aggravators: [],
      relievers: [],
      ...defaultValues,
    },
  });

  // Igual que en el triage: subscribe entrega el valor ya actualizado para guardar el borrador.
  const notifyChange = useEffectEvent((values: Partial<PainForm>) => onChange(values as PainDraft));
  useEffect(() => subscribe({ formState: { values: true }, callback: ({ values }) => notifyChange(values) }), [subscribe]);

  const onsetDate = useWatch({ control, name: "onsetDate" });
  const today = todayIso();
  const duration = isIsoDate(onsetDate) && onsetDate <= today ? describeDuration(daysBetween(onsetDate, today)) : null;
  const errorCount = Object.keys(errors).length;

  return (
    <form className={step.form} noValidate onSubmit={handleSubmit(onSubmit)}>
      <header className={step.encabezado}>
        <h1 className={step.titulo}>Cuéntanos de tu dolor</h1>
        <p className={step.intro}>Con esto distinguimos si se parece más a un dolor de nervio, de disco o de articulación.</p>
      </header>

      {isSubmitted && errorCount > 0 && (
        <p role="alert" className={step.resumenError}>
          {errorCount === 1 ? "Te falta 1 respuesta." : `Te faltan ${errorCount} respuestas.`}
        </p>
      )}

      <fieldset className={styles.seccion}>
        <legend className={styles.pregunta}>¿Qué tan fuerte es el dolor ahora?</legend>
        <p id="intensity-help" className={styles.ayuda}>
          0 es sin dolor y 10, el peor dolor que puedas imaginar.
        </p>
        <Controller
          control={control}
          name="intensity"
          render={({ field }) => (
            <PainScale
              name={field.name}
              value={field.value ?? null}
              onChange={field.onChange}
              describedBy="intensity-help"
              invalid={!!errors.intensity}
            />
          )}
        />
        <FieldError message={errors.intensity?.message} />
      </fieldset>

      <div className={styles.seccion}>
        <label htmlFor="onsetDate" className={styles.pregunta}>
          ¿Cuándo empezó esta vez?
        </label>
        <p id="onset-help" className={styles.ayuda}>
          Si ya te había pasado antes, cuenta desde que volvió.
        </p>
        <input
          id="onsetDate"
          type="date"
          max={today}
          className={styles.fecha}
          aria-describedby="onset-help"
          aria-invalid={errors.onsetDate ? true : undefined}
          {...register("onsetDate")}
        />
        {duration && <p className={styles.duracion}>{duration}</p>}
        <FieldError message={errors.onsetDate?.message} />
      </div>

      <fieldset className={styles.seccion}>
        <legend className={styles.pregunta}>¿Hasta dónde llega el dolor?</legend>
        <div className={styles.distribuciones}>
          {options.distributions?.map(({ id = "", label }) => (
            <label key={id} className={styles.distribucion}>
              <input type="radio" value={id} {...register("distribution")} />
              <span>
                <DistributionFigure distribution={id} />
                {label}
              </span>
            </label>
          ))}
        </div>
        <FieldError message={errors.distribution?.message} />
      </fieldset>

      <fieldset className={styles.seccion}>
        <legend className={styles.pregunta}>¿Cómo se siente?</legend>
        <p className={styles.ayuda}>Elige todas las que apliquen.</p>
        <Controller
          control={control}
          name="qualities"
          render={({ field }) => (
            <ChipGroup name={field.name} options={options.qualities ?? []} value={field.value} onChange={field.onChange} />
          )}
        />
        <FieldError message={errors.qualities?.message} />
      </fieldset>

      <fieldset className={styles.seccion}>
        <legend className={styles.pregunta}>¿Qué lo empeora?</legend>
        <p className={styles.ayuda}>Opcional. Elige todas las que apliquen.</p>
        <Controller
          control={control}
          name="aggravators"
          render={({ field }) => (
            <ChipGroup name={field.name} options={options.aggravators ?? []} value={field.value} onChange={field.onChange} />
          )}
        />
      </fieldset>

      <fieldset className={styles.seccion}>
        <legend className={styles.pregunta}>¿Qué lo alivia?</legend>
        <p className={styles.ayuda}>Opcional. Elige todas las que apliquen.</p>
        <Controller
          control={control}
          name="relievers"
          render={({ field }) => (
            <ChipGroup
              name={field.name}
              options={options.relievers ?? []}
              value={field.value}
              onChange={field.onChange}
              exclusive="NOTHING"
            />
          )}
        />
      </fieldset>

      <div className={step.acciones}>
        <button type="button" className={step.secundario} onClick={onBack}>
          Atrás
        </button>
        <button type="submit" className={step.principal}>
          Continuar
        </button>
      </div>
    </form>
  );
}

function FieldError({ message }: { message?: string }) {
  return message ? <p className={step.error}>{message}</p> : null;
}
