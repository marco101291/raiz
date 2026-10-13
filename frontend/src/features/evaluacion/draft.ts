import { z } from "zod";
import type { AssessmentRequest } from "@/lib/api/client";
import { daysBetween, todayIso } from "./dates";

/**
 * Borrador de la evaluación. Vive solo en el navegador (README: Raíz no guarda evaluaciones).
 * La fuente de verdad es la memoria; sessionStorage solo persiste entre recargas y puede fallar
 * (modo privado, almacenamiento bloqueado), así que todo acceso va en try/catch.
 */
const STORAGE_KEY = "raiz.evaluacion.v1";

const answer = z.enum(["si", "no"]);
export type Answer = z.infer<typeof answer>;

/** Respuestas del cuestionario de dolor mientras se llena: todo puede faltar. */
const painDraftSchema = z
  .object({
    intensity: z.number().int().min(0).max(10).nullable(),
    onsetDate: z.string(),
    distribution: z.string().nullable(),
    qualities: z.array(z.string()),
    aggravators: z.array(z.string()),
    relievers: z.array(z.string()),
  })
  .partial();
export type PainDraft = z.infer<typeof painDraftSchema>;

const draftSchema = z.object({
  step: z.enum(["alarma", "dolor", "mapa", "resultado"]),
  triage: z.record(z.string(), answer),
  pain: painDraftSchema.optional(),
  /** Zonas marcadas con su lado, p. ej. "GREAT_TOE-RIGHT" (mismo id que el path del mapa SVG). */
  sensoryZones: z.array(z.string().regex(/^[A-Z_]+-(RIGHT|LEFT)$/)).optional(),
});
export type Draft = z.infer<typeof draftSchema>;
export type StepId = Draft["step"];

export const emptyDraft: Draft = { step: "alarma", triage: {} };

let current: Draft | undefined;
const listeners = new Set<() => void>();

function readStorage(): Draft {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    // Lo guardado puede venir de otra versión o estar corrupto: si no valida, se empieza de cero.
    const parsed = raw ? draftSchema.safeParse(JSON.parse(raw)) : undefined;
    return parsed?.success ? parsed.data : emptyDraft;
  } catch {
    return emptyDraft;
  }
}

/** Para useSyncExternalStore: devuelve siempre la misma referencia mientras no cambie. */
export function getDraft(): Draft {
  current ??= readStorage();
  return current;
}

export function setDraft(next: Draft): void {
  current = next;
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next));
  } catch {
    // Sin almacenamiento el asistente sigue funcionando; solo se pierde el progreso al recargar.
  }
  listeners.forEach((listener) => listener());
}

export function resetDraft(): void {
  setDraft(emptyDraft);
}

export function subscribeDraft(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

/** Datos de alarma que la persona contestó con "sí", en el orden del catálogo. */
export function reportedRedFlags(triage: Draft["triage"]): string[] {
  return Object.entries(triage)
    .filter(([, value]) => value === "si")
    .map(([id]) => id);
}

/**
 * Convierte el borrador en la petición al motor. La duración se calcula aquí, al enviar, a partir de
 * la fecha de inicio. Si el cuestionario de dolor está incompleto no se envía (el motor lo acepta).
 */
export function buildRequest(draft: Draft, today = todayIso()): AssessmentRequest {
  const pain = draft.pain;
  const complete = pain?.intensity != null && pain.onsetDate && pain.distribution;
  return {
    redFlags: reportedRedFlags(draft.triage) as AssessmentRequest["redFlags"],
    pain: complete
      ? ({
          intensity: pain.intensity,
          durationDays: daysBetween(pain.onsetDate!, today),
          distribution: pain.distribution,
          qualities: pain.qualities ?? [],
          aggravators: pain.aggravators ?? [],
          relievers: pain.relievers ?? [],
        } as AssessmentRequest["pain"])
      : undefined,
    sensoryZones: (draft.sensoryZones ?? []).map((key) => {
      const i = key.lastIndexOf("-");
      return { finding: key.slice(0, i), side: key.slice(i + 1) };
    }) as AssessmentRequest["sensoryZones"],
  };
}
