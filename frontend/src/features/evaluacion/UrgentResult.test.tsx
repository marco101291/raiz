import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { UrgentResult } from "./UrgentResult";

const referral = (id: string, label: string) => ({
  urgency: { id, label },
  specialists: [{ id: "PHYSICIAN", label: "Médico" }],
  message: "Mensaje",
  disclaimer: "Aviso educativo",
});

describe("UrgentResult", () => {
  it("en urgencia muestra el 911", () => {
    render(
      <UrgentResult
        result={{ type: "URGENT_REFERRAL", referral: referral("EMERGENCY_NOW", "Acude a urgencias hoy"), reported: [], inferred: [] }}
        onEdit={vi.fn()}
      />,
    );
    expect(screen.getByText(/llama al 911/)).toBeInTheDocument();
    expect(screen.getByText("Aviso educativo")).toBeInTheDocument();
  });

  it("en valoración pronta no muestra el 911 y explica la alarma inferida", () => {
    render(
      <UrgentResult
        result={{
          type: "URGENT_REFERRAL",
          referral: referral("PROMPT", "Busca valoración médica en los próximos días"),
          reported: [],
          inferred: [{ id: "BILATERAL_SYMPTOMS", label: "Síntomas en ambas piernas o ambos brazos", emergency: true }],
        }}
        onEdit={vi.fn()}
      />,
    );
    expect(screen.queryByText(/911/)).not.toBeInTheDocument();
    expect(screen.getByText(/marcaste síntomas en ambos lados/)).toBeInTheDocument();
  });
});
