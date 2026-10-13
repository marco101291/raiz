import { z } from "zod";

/**
 * Borrador de la evaluación. Vive solo en el navegador (README: Raíz no guarda evaluaciones).
 * La fuente de verdad es la memoria; sessionStorage solo persiste entre recargas y puede fallar
 * (modo privado, almacenamiento bloqueado), así que todo acceso va en try/catch.
 */
const STORAGE_KEY = "raiz.evaluacion.v1";

const answer = z.enum(["si", "no"]);
export type Answer = z.infer<typeof answer>;

const draftSchema = z.object({
  step: z.enum(["alarma", "dolor"]),
  triage: z.record(z.string(), answer),
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
