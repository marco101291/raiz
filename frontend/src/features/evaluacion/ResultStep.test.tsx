import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { AssessmentResponse } from "@/lib/api/client";
import { ResultStep } from "./ResultStep";

const assess = vi.fn();
vi.mock("@/lib/api/client", () => ({ api: { assess: (b: unknown) => assess(b) } }));

const referral = (id: string, label: string) => ({
  urgency: { id, label },
  specialists: [{ id: "PHYSIATRIST", label: "Médico rehabilitador" }],
  message: "Mensaje de la derivación",
  disclaimer: "Esta herramienta es educativa.",
});

const notAssessed = () => ({ type: "NOT_ASSESSED" as const, features: [], explanation: "" });

const WARNING_SIGNS = [
  { id: "BLADDER_OR_BOWEL_DYSFUNCTION", label: "Dificultad para orinar", emergency: true },
];

function renderResult(request = {}) {
  const onEdit = vi.fn();
  const onRestart = vi.fn();
  render(<ResultStep request={request} warningSigns={WARNING_SIGNS} onEdit={onEdit} onRestart={onRestart} />);
  return { onEdit, onRestart };
}

describe("ResultStep", () => {
  beforeEach(() => assess.mockReset());

  it("correlación: qué hacer, cuándo ir a urgencias, qué puede ser y, al final, las raíces para el médico", async () => {
    assess.mockResolvedValue({
      type: "ROOT_CORRELATION",
      referral: referral("PROMPT", "Busca valoración médica en los próximos días"),
      pattern: {
        type: "RADICULAR",
        chronicity: { id: "ACUTE", label: "Agudo (menos de 6 semanas)" },
        features: ["Dolor descrito como: ardor o quemazón"],
        explanation: "Este patrón es compatible con irritación de una raíz nerviosa.",
      },
      matches: [
        {
          rootCode: "S1",
          side: "RIGHT",
          label: "S1 derecha",
          score: 5,
          neurologicalDeficit: true,
          evidence: [
            { kind: "SENSORY", finding: "SOLE", weight: 2, explanation: "Síntomas sensitivos en: planta del pie" },
            { kind: "MOTOR", finding: "ANKLE_PLANTAR_FLEXION", weight: 3, explanation: "Debilidad al: pararse de puntas" },
          ],
          priorHerniationLevels: [],
        },
      ],
      otherCauses: [],
    } satisfies AssessmentResponse);
    renderResult({ redFlags: [] });

    const headings = (await screen.findAllByRole("heading")).map((h) => h.textContent);
    expect(headings).toEqual([
      "Busca valoración médica en los próximos días",
      "A quién acudir",
      "Cuándo ir a urgencias",
      "Qué puede ser",
      "Cómo se comporta tu dolor",
      "S1 derecha",
    ]);
    expect(screen.getByText("Dificultad para orinar")).toBeInTheDocument();
    const forDoctor = screen.getByText("Para tu médico: raíces nerviosas compatibles").closest("details");
    expect(forDoctor).not.toHaveAttribute("open");
    expect(forDoctor).toContainElement(screen.getByRole("heading", { name: "S1 derecha" }));
    expect(screen.getByText("Hay datos de pérdida de fuerza o de reflejos.")).toBeInTheDocument();
    expect(screen.getByText("Debilidad al: pararse de puntas")).toBeInTheDocument();
    expect(screen.getByText("Agudo (menos de 6 semanas)")).toBeInTheDocument();
    expect(screen.getByText("Esta herramienta es educativa.")).toBeInTheDocument();
    expect(screen.queryByText(/911/)).not.toBeInTheDocument();
  });

  it("sin correlación: muestra la nota del motor", async () => {
    assess.mockResolvedValue({
      type: "NO_ROOT_CORRELATION",
      referral: referral("ROUTINE", "Agenda una valoración con un especialista"),
      pattern: { type: "DISCOGENIC", features: [], explanation: "Compatible con dolor de origen discal." },
      note: "Lo que describes no sigue el territorio de una raíz nerviosa.",
      otherCauses: [],
    } satisfies AssessmentResponse);
    renderResult();

    expect(await screen.findByRole("heading", { name: "Qué puede ser" })).toBeInTheDocument();
    expect(screen.getByText("Lo que describes no sigue el territorio de una raíz nerviosa.")).toBeInTheDocument();
    expect(screen.queryByText(/Para tu médico/)).not.toBeInTheDocument();
  });

  it("muestra otras causas posibles de las zonas marcadas, con su fuente", async () => {
    assess.mockResolvedValue({
      type: "NO_ROOT_CORRELATION",
      referral: referral("PROMPT", "Busca valoración médica en los próximos días"),
      pattern: notAssessed(),
      note: "Nota",
      otherCauses: [
        { id: "PUBALGIA", title: "Pubalgia", description: "Dolor en el pubis.", sourceReference: "Weir 2015" },
      ],
    } satisfies AssessmentResponse);
    renderResult();

    expect(
      await screen.findByRole("heading", { name: "Otras causas frecuentes en las zonas que marcaste" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Pubalgia" })).toBeInTheDocument();
    expect(screen.getByText("Fuente: Weir 2015")).toBeInTheDocument();
  });

  it("para el médico, señala la raíz que coincide con la hernia previa", async () => {
    assess.mockResolvedValue({
      type: "ROOT_CORRELATION",
      referral: referral("ROUTINE", "Agenda"),
      pattern: notAssessed(),
      matches: [
        {
          rootCode: "L5",
          side: "RIGHT",
          label: "L5 derecha",
          score: 2,
          neurologicalDeficit: false,
          evidence: [],
          priorHerniationLevels: ["L4-L5"],
        },
      ],
      otherCauses: [],
    } satisfies AssessmentResponse);
    renderResult();

    expect(await screen.findByText("Coincide con tu hernia previa L4-L5.")).toBeInTheDocument();
  });

  it("muestra lo que conviene comentar en la consulta", async () => {
    assess.mockResolvedValue({
      type: "NO_ROOT_CORRELATION",
      referral: referral("ROUTINE", "Agenda"),
      pattern: notAssessed(),
      note: "Nota",
      otherCauses: [],
    } satisfies AssessmentResponse);
    render(
      <ResultStep request={{}} notes={["No pudiste hacer «Caminar de talones» por dolor."]} onEdit={vi.fn()} onRestart={vi.fn()} />,
    );

    expect(await screen.findByRole("heading", { name: "Coméntalo en tu consulta" })).toBeInTheDocument();
    expect(screen.getByText("No pudiste hacer «Caminar de talones» por dolor.")).toBeInTheDocument();
  });

  it("alarma: solo la derivación, el 911 y por qué; sin patrón ni raíces", async () => {
    assess.mockResolvedValue({
      type: "URGENT_REFERRAL",
      referral: referral("EMERGENCY_NOW", "Acude a urgencias hoy"),
      pattern: notAssessed(),
      reported: [],
      inferred: [{ id: "BILATERAL_SYMPTOMS", label: "Síntomas en ambas piernas o ambos brazos", emergency: true }],
    } satisfies AssessmentResponse);
    renderResult({ redFlags: ["SADDLE_ANESTHESIA"] });

    expect(await screen.findByRole("heading", { level: 1, name: "Acude a urgencias hoy" })).toBeInTheDocument();
    expect(screen.getByText(/llama al 911/)).toBeInTheDocument();
    expect(screen.getByText(/marcaste síntomas en ambos lados/)).toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: "Cómo se comporta tu dolor" })).not.toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: "Cuándo ir a urgencias" })).not.toBeInTheDocument();
  });

  it("si el motor falla con alarma, no deja a la persona sin salida", async () => {
    assess.mockRejectedValueOnce(new Error("503")).mockResolvedValueOnce({
      type: "URGENT_REFERRAL",
      referral: referral("EMERGENCY_NOW", "Acude a urgencias hoy"),
      pattern: notAssessed(),
      reported: [],
      inferred: [],
    } satisfies AssessmentResponse);
    renderResult({ redFlags: ["SADDLE_ANESTHESIA"] });

    expect(await screen.findByRole("alert")).toHaveTextContent("acude a urgencias o llama al 911");
    await userEvent.click(screen.getByRole("button", { name: "Reintentar" }));

    expect(await screen.findByRole("heading", { name: "Acude a urgencias hoy" })).toBeInTheDocument();
    expect(assess).toHaveBeenCalledTimes(2);
  });

  it("los botones permiten corregir o empezar de nuevo", async () => {
    assess.mockResolvedValue({
      type: "NO_ROOT_CORRELATION",
      referral: referral("ROUTINE", "Agenda"),
      pattern: notAssessed(),
      note: "Nota",
      otherCauses: [],
    } satisfies AssessmentResponse);
    const { onEdit, onRestart } = renderResult();

    await userEvent.click(await screen.findByRole("button", { name: "Cambiar mis respuestas" }));
    await userEvent.click(screen.getByRole("button", { name: "Empezar de nuevo" }));

    expect(onEdit).toHaveBeenCalledOnce();
    expect(onRestart).toHaveBeenCalledOnce();
  });
});
