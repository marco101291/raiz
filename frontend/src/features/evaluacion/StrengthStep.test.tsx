import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { StrengthStep } from "./StrengthStep";

const tests = [
  {
    id: "HEEL_WALK",
    name: "Caminar de talones",
    instructions: "Da 10 pasos con la punta del pie levantada.",
    safetyNote: "Apóyate en una pared y detente si el dolor sube claramente.",
    motorAction: { id: "ANKLE_DORSIFLEXION", label: "Levantar la punta del pie" },
  },
];
const questions = [
  {
    id: "HAND_GRIP",
    question: "¿Te falta fuerza para apretar con la mano?",
    motorAction: { id: "FINGER_FLEXION", label: "Cerrar el puño con fuerza" },
  },
];

function setup(props: Partial<Parameters<typeof StrengthStep>[0]> = {}) {
  const onSubmit = vi.fn();
  const onChange = vi.fn();
  render(
    <StrengthStep
      tests={tests}
      questions={questions}
      defaultValues={{ tests: {}, functional: {} }}
      onChange={onChange}
      onBack={vi.fn()}
      onSubmit={onSubmit}
      {...props}
    />,
  );
  return { onSubmit, onChange };
}

describe("StrengthStep", () => {
  it("muestra pruebas de pierna con instrucciones y aviso de seguridad, y preguntas de brazo", () => {
    setup();

    const heelWalk = screen.getByRole("group", { name: "Caminar de talones" });
    expect(within(heelWalk).getByText("Da 10 pasos con la punta del pie levantada.")).toBeInTheDocument();
    expect(screen.getByText(/Apóyate en una pared/)).toBeInTheDocument();
    expect(within(heelWalk).getByRole("radio", { name: "No la hice" })).toBeInTheDocument();
    expect(screen.getByRole("group", { name: "¿Te falta fuerza para apretar con la mano?" })).toBeInTheDocument();
  });

  it("sin región de cuello no hay preguntas de brazo", () => {
    setup({ questions: [] });

    expect(screen.queryByRole("heading", { name: "Tus brazos" })).not.toBeInTheDocument();
  });

  it("pide una respuesta por prueba y por pregunta antes de seguir", async () => {
    const { onSubmit } = setup();

    await userEvent.click(screen.getByRole("button", { name: "Ver mi resultado" }));
    expect(screen.getByRole("alert")).toHaveTextContent("Te faltan 2 respuestas.");
    expect(onSubmit).not.toHaveBeenCalled();

    const heelWalk = screen.getByRole("group", { name: "Caminar de talones" });
    await userEvent.click(within(heelWalk).getByRole("radio", { name: "No pude por dolor" }));
    const grip = screen.getByRole("group", { name: "¿Te falta fuerza para apretar con la mano?" });
    await userEvent.click(within(grip).getByRole("radio", { name: "Sí, el brazo izquierdo" }));
    await userEvent.click(screen.getByRole("button", { name: "Ver mi resultado" }));

    expect(onSubmit).toHaveBeenCalledWith({ tests: { HEEL_WALK: "PAIN" }, functional: { HAND_GRIP: "LEFT" } });
  });
});
