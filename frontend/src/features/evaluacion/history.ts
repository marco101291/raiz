import type { Schemas } from "@/lib/api/client";
import type { PainDraft } from "./draft";

/**
 * History the result shows under "Coméntalo en tu consulta": it does not change the engine's decision, but a
 * professional should know it (a prior herniation, a spine operation).
 */
export function historyNotes(pain: PainDraft | undefined, discLevels: Schemas["DiscLevelView"][]): string[] {
  const notes: string[] = [];
  if (pain?.priorHerniation === "YES") {
    const labels = discLevels.filter((level) => pain.herniationLevels?.includes(level.id)).map((l) => l.label);
    notes.push(`Te diagnosticaron una hernia de disco antes${labels.length > 0 ? ` (${labels.join(", ")})` : ""}.`);
  }
  if (pain?.priorSpineSurgery === "YES") {
    notes.push("Te han operado de la columna.");
  }
  return notes;
}
