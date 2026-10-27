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
    /** History: "YES", "NO" or "UNKNOWN"; levels like "L4_L5" only if "YES"; surgery "YES" or "NO". */
    priorHerniation: z.string().nullable(),
    herniationLevels: z.array(z.string()),
    priorSpineSurgery: z.string().nullable(),
  })
  .partial();
export type PainDraft = z.infer<typeof painDraftSchema>;

const draftSchema = z.object({
  step: z.enum(["alarma", "dolor", "mapa", "fuerza", "resultado"]),
  triage: z.record(z.string(), answer),
  pain: painDraftSchema.optional(),
  /** Marked zones with their side, e.g. "GREAT_TOE-RIGHT" (same id as the SVG map path). */
  sensoryZones: z.array(z.string().regex(/^[A-Z_]+-(RIGHT|LEFT)$/)).optional(),
  /** Strength test id → answer, and functional question id → answer (step 4). */
  strength: z.record(z.string(), z.string()).optional(),
  functional: z.record(z.string(), z.string()).optional(),
  /** Weakness derived from the strength answers, e.g. "ANKLE_DORSIFLEXION-RIGHT". */
  weakness: z.array(z.string().regex(/^[A-Z_]+-(RIGHT|LEFT)$/)).optional(),
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

/** Guided strength applies only if the pain radiates or zones were marked (README, Forms). */
export function strengthApplies(draft: Pick<Draft, "pain" | "sensoryZones">): boolean {
  return !painStaysAxial(draft.pain) || (draft.sensoryZones?.length ?? 0) > 0;
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
    sensoryZones: (draft.sensoryZones ?? []).map(sided) as AssessmentRequest["sensoryZones"],
    weakness: (draft.weakness ?? []).map(sided) as AssessmentRequest["weakness"],
    history: historyFrom(pain),
  };
}

type History = NonNullable<AssessmentRequest["history"]>;

/** History is optional: sent only if at least one of its questions was answered. */
function historyFrom(pain: PainDraft | undefined): History | undefined {
  if (!pain?.priorHerniation && !pain?.priorSpineSurgery) {
    return undefined;
  }
  const herniation = pain.priorHerniation ?? "UNKNOWN";
  return {
    priorHerniation: herniation,
    // Levels only make sense for a diagnosed herniation; the engine rejects them otherwise.
    herniationLevels: herniation === "YES" ? (pain.herniationLevels ?? []) : [],
    priorSpineSurgery: pain.priorSpineSurgery === "YES",
  } as History;
}

/** "GREAT_TOE-RIGHT" → { finding: "GREAT_TOE", side: "RIGHT" }. The last "-" separates the side. */
function sided(key: string) {
  const i = key.lastIndexOf("-");
  return { finding: key.slice(0, i), side: key.slice(i + 1) };
}
