import { describe, expect, it } from "vitest";
import { historyNotes } from "./history";

const levels = [
  { id: "L4_L5", label: "L4-L5", region: "LUMBOSACRAL" },
  { id: "L5_S1", label: "L5-S1", region: "LUMBOSACRAL" },
];

describe("historyNotes", () => {
  it("anota la hernia previa con sus niveles y la cirugía", () => {
    const pain = { priorHerniation: "YES", herniationLevels: ["L5_S1", "L4_L5"], priorSpineSurgery: "YES" };
    expect(historyNotes(pain, levels)).toEqual([
      "Te diagnosticaron una hernia de disco antes (L4-L5, L5-S1).",
      "Te han operado de la columna.",
    ]);
  });

  it("sin niveles, sin paréntesis; con «no» o «no sé», nada", () => {
    expect(historyNotes({ priorHerniation: "YES" }, levels)).toEqual(["Te diagnosticaron una hernia de disco antes."]);
    expect(historyNotes({ priorHerniation: "UNKNOWN", priorSpineSurgery: "NO" }, levels)).toEqual([]);
  });
});
