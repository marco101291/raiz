import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { TriageStep } from "./TriageStep";

const flags = [
  { id: "SADDLE_ANESTHESIA", label: "Adormecimiento en zona genital", emergency: true },
  { id: "FEVER", label: "Fiebre asociada al dolor", emergency: false },
];

function setup(defaultValues = {}) {
  const onSubmit = vi.fn();
  const onChange = vi.fn();
  render(<TriageStep flags={flags} defaultValues={defaultValues} onChange={onChange} onSubmit={onSubmit} />);
  const answer = (label: RegExp, value: "Sí" | "No") =>
    userEvent.click(within(screen.getByRole("group", { name: label })).getByRole("radio", { name: value }));
  return { onSubmit, onChange, answer, user: userEvent.setup() };
}

describe("TriageStep", () => {
  it("no deja avanzar sin responder todas las preguntas", async () => {
    const { onSubmit } = setup();

    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("Te faltan 2 preguntas por responder.");
    expect(screen.getAllByText("Elige sí o no.")).toHaveLength(2);
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it("todo en no: continúa sin datos de alarma", async () => {
    const { onSubmit, answer } = setup();

    await answer(/Adormecimiento/, "No");
    await answer(/Fiebre/, "No");
    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));

    expect(onSubmit).toHaveBeenCalledWith([]);
  });

  it("con un sí, el botón cambia a 'Ver qué hacer' y envía ese dato", async () => {
    const { onSubmit, answer } = setup();

    await answer(/Adormecimiento/, "No");
    await answer(/Fiebre/, "Sí");
    await userEvent.click(screen.getByRole("button", { name: "Ver qué hacer" }));

    expect(onSubmit).toHaveBeenCalledWith(["FEVER"]);
  });

  it("restaura las respuestas guardadas y avisa de cada cambio", async () => {
    const { onChange, answer } = setup({ FEVER: "si" });

    expect(within(screen.getByRole("group", { name: /Fiebre/ })).getByRole("radio", { name: "Sí" })).toBeChecked();

    await answer(/Adormecimiento/, "No");

    expect(onChange).toHaveBeenLastCalledWith({ FEVER: "si", SADDLE_ANESTHESIA: "no" });
  });
});
