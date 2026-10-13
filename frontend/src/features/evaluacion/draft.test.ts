import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

// draft.ts guarda estado a nivel de módulo: cada prueba importa una copia nueva.
async function freshDraft() {
  vi.resetModules();
  return import("./draft");
}

describe("draft", () => {
  beforeEach(() => sessionStorage.clear());
  afterEach(() => vi.restoreAllMocks());

  it("empieza vacío si no hay nada guardado", async () => {
    const { getDraft, emptyDraft } = await freshDraft();
    expect(getDraft()).toEqual(emptyDraft);
  });

  it("recupera lo guardado en una recarga", async () => {
    sessionStorage.setItem("raiz.evaluacion.v1", JSON.stringify({ step: "dolor", triage: { FEVER: "no" } }));
    const { getDraft } = await freshDraft();
    expect(getDraft()).toEqual({ step: "dolor", triage: { FEVER: "no" } });
  });

  it("descarta lo guardado si está corrupto o es de otra versión", async () => {
    sessionStorage.setItem("raiz.evaluacion.v1", JSON.stringify({ step: "resultado", triage: { FEVER: "tal vez" } }));
    const { getDraft, emptyDraft } = await freshDraft();
    expect(getDraft()).toEqual(emptyDraft);
  });

  it("sigue funcionando en memoria si sessionStorage falla", async () => {
    vi.spyOn(Storage.prototype, "setItem").mockImplementation(() => {
      throw new DOMException("bloqueado", "SecurityError");
    });
    const { getDraft, setDraft } = await freshDraft();
    const listener = vi.fn();
    const { subscribeDraft } = await import("./draft");
    subscribeDraft(listener);

    setDraft({ step: "dolor", triage: { FEVER: "si" } });

    expect(getDraft()).toEqual({ step: "dolor", triage: { FEVER: "si" } });
    expect(listener).toHaveBeenCalledOnce();
  });

  it("reportedRedFlags devuelve solo los contestados con sí", async () => {
    const { reportedRedFlags } = await freshDraft();
    expect(reportedRedFlags({ FEVER: "si", CANCER_HISTORY: "no", SADDLE_ANESTHESIA: "si" })).toEqual([
      "FEVER",
      "SADDLE_ANESTHESIA",
    ]);
  });
});

describe("buildRequest", () => {
  it("calcula la duración desde la fecha de inicio al momento de enviar", async () => {
    const { buildRequest } = await freshDraft();
    const request = buildRequest(
      {
        step: "resultado",
        triage: { FEVER: "no" },
        pain: {
          intensity: 4,
          onsetDate: "2026-10-04",
          distribution: "PROXIMAL",
          qualities: ["BURNING"],
          aggravators: ["SITTING"],
          relievers: [],
        },
      },
      "2026-10-13",
    );
    expect(request).toEqual({
      redFlags: [],
      pain: {
        intensity: 4,
        durationDays: 9,
        distribution: "PROXIMAL",
        qualities: ["BURNING"],
        aggravators: ["SITTING"],
        relievers: [],
      },
    });
  });

  it("no envía un cuestionario de dolor incompleto", async () => {
    const { buildRequest } = await freshDraft();
    expect(buildRequest({ step: "resultado", triage: {}, pain: { intensity: 4 } }).pain).toBeUndefined();
  });
});
