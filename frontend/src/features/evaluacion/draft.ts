import { z } from "zod";
import type { AssessmentRequest } from "@/lib/api/client";
import { daysBetween, todayIso } from "./dates";

/**
 * Assessment draft. It lives only in the browser (README: Raíz does not store assessments).
 * Memory is the source of truth; sessionStorage only persists across reloads and can fail (private mode,
 * blocked storage), so every access is wrapped in try/catch.
 */
const STORAGE_KEY = "raiz.evaluacion.v1";

const answer = z.enum(["si", "no"]);
export type Answer = z.infer<typeof answer>;

/** Pain questionnaire answers while it is being filled in: anything may be missing. */
const painDraftSchema = z
  .object({
    intensity: z.number().int().min(0).max(10).nullable(),
    onsetDate: z.string(),
    /** Where the pain starts: "CERVICAL", "LUMBOSACRAL" or both. */
    regions: z.array(z.string()),
    /** How far it reaches from each region, e.g. { LUMBOSACRAL: "DISTAL" }. */
    reach: z.record(z.string(), z.string().nullable()),
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
  /** Marked zones with their side, e.g. "GREAT_TOE-RIGHT" (same id as the SVG map path). */
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
    // Stored data may come from another version or be corrupt: if it does not validate, start over.
    const parsed = raw ? draftSchema.safeParse(JSON.parse(raw)) : undefined;
    return parsed?.success ? parsed.data : emptyDraft;
  } catch {
    return emptyDraft;
  }
}

/** For useSyncExternalStore: always returns the same reference until it changes. */
export function getDraft(): Draft {
  current ??= readStorage();
  return current;
}

export function setDraft(next: Draft): void {
  current = next;
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next));
  } catch {
    // Without storage the wizard still works; only progress is lost on reload.
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

/** Red flags the person answered "sí" to, in catalog order. */
/** The map is optional when the pain stays in the neck or the low back in every region (README, Forms). */
export function painStaysAxial(pain: PainDraft | undefined): boolean {
  const regions = pain?.regions ?? [];
  return regions.length > 0 && regions.every((region) => pain?.reach?.[region] === "AXIAL_ONLY");
}

export function reportedRedFlags(triage: Draft["triage"]): string[] {
  return Object.entries(triage)
    .filter(([, value]) => value === "si")
    .map(([id]) => id);
}

/**
 * Turns the draft into the engine request. The duration is computed here, on submit, from the start date.
 * If the pain questionnaire is incomplete it is not sent (the engine accepts that).
 */
export function buildRequest(draft: Draft, today = todayIso()): AssessmentRequest {
  const pain = draft.pain;
  const regions = pain?.regions ?? [];
  const complete =
    pain?.intensity != null && pain.onsetDate && regions.length > 0 && regions.every((region) => pain.reach?.[region]);
  return {
    redFlags: reportedRedFlags(draft.triage) as AssessmentRequest["redFlags"],
    pain: complete
      ? ({
          intensity: pain.intensity,
          durationDays: daysBetween(pain.onsetDate!, today),
          reach: regions.map((region) => ({ region, distribution: pain.reach![region] })),
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
