import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

const redFlags = vi.fn();
const painOptions = vi.fn();
const assess = vi.fn();
vi.mock("@/lib/api/client", () => ({
  api: { redFlags: () => redFlags(), painOptions: () => painOptions(), assess: (b: unknown) => assess(b) },
}));

const options = {
  qualities: [
    { id: "BURNING", label: "Ardor o quemazón" },
    { id: "DULL_ACHE", label: "Dolor sordo, como cansancio" },
  ],
  aggravators: [{ id: "SITTING", label: "Estar sentado" }],
  relievers: [
    { id: "LYING_DOWN", label: "Acostarse" },
    { id: "NOTHING", label: "Nada lo alivia" },
  ],
  distributions: [
    { id: "AXIAL_ONLY", label: "Solo en la espalda o el cuello" },
    { id: "PROXIMAL", label: "Se extiende a nalga o muslo" },
    { id: "DISTAL", label: "Baja por debajo de la rodilla" },
  ],
};

const flags = [
  { id: "SADDLE_ANESTHESIA", label: "Adormecimiento en zona genital", emergency: true },
  { id: "FEVER", label: "Fiebre asociada al dolor", emergency: false },
];

async function renderWizard() {
  vi.resetModules(); // borrador nuevo en cada prueba
  const { Wizard } = await import("./Wizard");
  render(<Wizard />);
  await screen.findByRole("heading", { name: /te pasa alguna de estas cosas/ });
}

const answer = (label: RegExp, value: "Sí" | "No") =>
  userEvent.click(within(screen.getByRole("group", { name: label })).getByRole("radio", { name: value }));

describe("Wizard", () => {
  beforeEach(() => {
    redFlags.mockReset().mockResolvedValue(flags);
    painOptions.mockReset().mockResolvedValue(options);
    assess.mockReset();
  });

  it("con alarma envía en ese momento y muestra solo la derivación", async () => {
    assess.mockResolvedValue({
      type: "URGENT_REFERRAL",
      referral: {
        urgency: { id: "EMERGENCY_NOW", label: "Acude a urgencias hoy" },
        specialists: [{ id: "EMERGENCY_ROOM", label: "Servicio de urgencias" }],
        message: "No esperes.",
        disclaimer: "Esta herramienta es educativa.",
      },
      pattern: { type: "NOT_ASSESSED" },
      reported: [flags[0]],
      inferred: [],
    });
    await renderWizard();

    await answer(/Adormecimiento/, "Sí");
    await answer(/Fiebre/, "No");
    await userEvent.click(screen.getByRole("button", { name: "Ver qué hacer" }));

    expect(assess).toHaveBeenCalledWith({ redFlags: ["SADDLE_ANESTHESIA"] });
    expect(await screen.findByRole("heading", { level: 1, name: "Acude a urgencias hoy" })).toBeInTheDocument();
    expect(screen.getByText(/llama al 911/)).toBeInTheDocument();
    expect(screen.queryByRole("navigation", { name: "Progreso de la evaluación" })).not.toBeInTheDocument();
  });

  it("sin alarma pasa al cuestionario de dolor sin llamar al motor", async () => {
    await renderWizard();

    await answer(/Adormecimiento/, "No");
    await answer(/Fiebre/, "No");
    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));

    expect(await screen.findByRole("heading", { name: "Cuéntanos de tu dolor" })).toBeInTheDocument();
    expect(screen.getByText("Paso 2 de 5:", { exact: false })).toBeInTheDocument();
    expect(assess).not.toHaveBeenCalled();
  });

  it("si el catálogo no carga, explica qué pasó y permite reintentar", async () => {
    redFlags.mockRejectedValueOnce(new Error("sin red"));
    vi.resetModules();
    const { Wizard } = await import("./Wizard");
    render(<Wizard />);

    expect(await screen.findByRole("alert")).toHaveTextContent("No pudimos cargar las preguntas");
    expect(painOptions).toHaveBeenCalled();
    await userEvent.click(screen.getByRole("button", { name: "Reintentar" }));

    expect(await screen.findByRole("heading", { name: /te pasa alguna de estas cosas/ })).toBeInTheDocument();
  });
});

describe("Wizard: cuestionario de dolor", () => {
  beforeEach(() => {
    redFlags.mockReset().mockResolvedValue(flags);
    painOptions.mockReset().mockResolvedValue(options);
    sessionStorage.setItem("raiz.evaluacion.v1", JSON.stringify({ step: "dolor", triage: { FEVER: "no" } }));
  });

  it("pide los campos obligatorios y luego avanza al mapa guardando las respuestas", async () => {
    vi.resetModules();
    const { Wizard } = await import("./Wizard");
    render(<Wizard />);
    await screen.findByRole("heading", { name: "Cuéntanos de tu dolor" });

    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Te faltan 4 respuestas.");

    await userEvent.click(screen.getByRole("radio", { name: "6: Moderado" }));
    await userEvent.type(screen.getByLabelText("¿Cuándo empezó esta vez?"), "2026-01-05");
    await userEvent.click(screen.getByRole("radio", { name: /Baja por debajo de la rodilla/ }));
    await userEvent.click(screen.getByRole("checkbox", { name: "Ardor o quemazón" }));
    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));

    expect(await screen.findByRole("heading", { name: "Mapa del cuerpo" })).toBeInTheDocument();
    const saved = JSON.parse(sessionStorage.getItem("raiz.evaluacion.v1") ?? "{}");
    expect(saved.pain).toMatchObject({
      intensity: 6,
      onsetDate: "2026-01-05",
      distribution: "DISTAL",
      qualities: ["BURNING"],
    });
  });

  it("'Nada lo alivia' excluye a las demás opciones y al revés", async () => {
    vi.resetModules();
    const { Wizard } = await import("./Wizard");
    render(<Wizard />);
    const lying = await screen.findByRole("checkbox", { name: "Acostarse" });
    const nothing = screen.getByRole("checkbox", { name: "Nada lo alivia" });

    await userEvent.click(lying);
    await userEvent.click(nothing);
    expect(nothing).toBeChecked();
    expect(lying).not.toBeChecked();

    await userEvent.click(lying);
    expect(lying).toBeChecked();
    expect(nothing).not.toBeChecked();
  });

  it("rechaza una fecha de inicio futura", async () => {
    vi.resetModules();
    const { Wizard } = await import("./Wizard");
    render(<Wizard />);
    await userEvent.type(await screen.findByLabelText("¿Cuándo empezó esta vez?"), "2999-01-01");
    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));

    expect(await screen.findByText("La fecha no puede ser posterior a hoy.")).toBeInTheDocument();
  });
});
