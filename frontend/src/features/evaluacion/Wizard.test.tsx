import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

const redFlags = vi.fn();
const assess = vi.fn();
vi.mock("@/lib/api/client", () => ({ api: { redFlags: () => redFlags(), assess: (b: unknown) => assess(b) } }));

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

    expect(await screen.findByRole("heading", { name: "Tu dolor" })).toBeInTheDocument();
    expect(screen.getByText("Paso 2 de 5:", { exact: false })).toBeInTheDocument();
    expect(assess).not.toHaveBeenCalled();
  });

  it("si el catálogo no carga, explica qué pasó y permite reintentar", async () => {
    redFlags.mockRejectedValueOnce(new Error("sin red"));
    vi.resetModules();
    const { Wizard } = await import("./Wizard");
    render(<Wizard />);

    expect(await screen.findByRole("alert")).toHaveTextContent("No pudimos cargar las preguntas");
    await userEvent.click(screen.getByRole("button", { name: "Reintentar" }));

    expect(await screen.findByRole("heading", { name: /te pasa alguna de estas cosas/ })).toBeInTheDocument();
  });
});
