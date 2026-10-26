import { fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { MapStep, splitKey } from "./MapStep";

const zones = [
  { id: "THUMB", label: "Pulgar", region: { id: "CERVICAL", label: "Cervical" } },
  { id: "GREAT_TOE", label: "Dedo gordo del pie", region: { id: "LUMBOSACRAL", label: "Lumbosacra" } },
  { id: "SOLE", label: "Planta del pie", region: { id: "LUMBOSACRAL", label: "Lumbosacra" } },
];

function setup(optional = false, defaultValue: string[] = [], regions: string[] = []) {
  const onSubmit = vi.fn();
  const onChange = vi.fn();
  const { container } = render(
    <MapStep
      zones={zones}
      optional={optional}
      regions={regions}
      defaultValue={defaultValue}
      onChange={onChange}
      onBack={vi.fn()}
      onSubmit={onSubmit}
    />,
  );
  const path = (key: string) => container.querySelector(`[data-zone="${key}"]`)!;
  return { onSubmit, onChange, path, container };
}

describe("MapStep", () => {
  it("el dibujo y la lista comparten la selección", async () => {
    const { path, onChange } = setup();
    const checkbox = screen.getByRole("checkbox", { name: "Dedo gordo del pie, derecha" });

    fireEvent.click(path("GREAT_TOE-RIGHT"));
    expect(checkbox).toBeChecked();
    expect(onChange).toHaveBeenLastCalledWith(["GREAT_TOE-RIGHT"]);
    expect(screen.getByRole("heading", { name: "Marcaste 1 zona" })).toBeInTheDocument();

    await userEvent.click(checkbox);
    expect(path("GREAT_TOE-RIGHT")).not.toHaveClass(/marcada/);
    expect(onChange).toHaveBeenLastCalledWith([]);
  });

  it("cada zona marcada se puede quitar desde la lista de marcadas", async () => {
    const { onChange } = setup(false, ["SOLE-LEFT", "THUMB-RIGHT"]);

    await userEvent.click(screen.getByRole("button", { name: "Quitar Planta del pie, izquierda" }));

    expect(onChange).toHaveBeenLastCalledWith(["THUMB-RIGHT"]);
    expect(screen.getByRole("checkbox", { name: "Planta del pie, izquierda" })).not.toBeChecked();
  });

  it("si el dolor se irradia, pide al menos una zona", async () => {
    const { onSubmit } = setup(false);

    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));

    expect(screen.getByRole("alert")).toHaveTextContent("Marca al menos una zona");
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it("si el dolor no se irradia, el mapa es opcional", async () => {
    const { onSubmit } = setup(true);

    await userEvent.click(screen.getByRole("button", { name: "Continuar sin marcar" }));

    expect(onSubmit).toHaveBeenCalledWith([]);
  });

  it("de frente, la derecha del paciente queda a la izquierda de la pantalla; de espalda, al revés", () => {
    const { path } = setup();
    // The limb on the left of the screen is the unmirrored group.
    const onScreenLeft = (key: string) => path(key).closest("g")?.getAttribute("transform") === null;

    expect(onScreenLeft("GREAT_TOE-RIGHT")).toBe(true);
    expect(onScreenLeft("GREAT_TOE-LEFT")).toBe(false);
    expect(onScreenLeft("SOLE-LEFT")).toBe(true);
    expect(onScreenLeft("SOLE-RIGHT")).toBe(false);
  });

  it("si el dolor empieza en el cuello, solo muestra el brazo y la mano, de frente", () => {
    const { path } = setup(false, [], ["CERVICAL"]);

    expect(screen.getByRole("checkbox", { name: "Pulgar, derecha" })).toBeInTheDocument();
    expect(screen.queryByRole("checkbox", { name: "Dedo gordo del pie, derecha" })).not.toBeInTheDocument();
    expect(screen.queryByRole("table", { name: "Pierna y pie" })).not.toBeInTheDocument();
    expect(path("THUMB-RIGHT")).not.toBeNull();
    expect(path("GREAT_TOE-RIGHT")).toBeNull();
    expect(screen.queryByText("De espalda")).not.toBeInTheDocument();
    expect(screen.getByText("Toca las zonas del brazo y la mano donde lo sientes.")).toBeInTheDocument();
  });

  it("si el dolor empieza en la espalda baja, muestra la pierna de frente y de espalda", () => {
    setup(false, [], ["LUMBOSACRAL"]);

    expect(screen.queryByRole("checkbox", { name: "Pulgar, derecha" })).not.toBeInTheDocument();
    expect(screen.getByText("De frente")).toBeInTheDocument();
    expect(screen.getByText("De espalda")).toBeInTheDocument();
  });

  it("descarta zonas marcadas antes en una región que ya no aplica", async () => {
    const { onSubmit } = setup(false, ["THUMB-RIGHT", "SOLE-LEFT"], ["LUMBOSACRAL"]);

    expect(screen.getByRole("heading", { name: "Marcaste 1 zona" })).toBeInTheDocument();
    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));

    expect(onSubmit).toHaveBeenCalledWith(["SOLE-LEFT"]);
  });

  it("splitKey separa la zona del lado aunque la zona tenga guiones bajos", () => {
    expect(splitKey("UPPER_ANTERIOR_THIGH-LEFT")).toEqual(["UPPER_ANTERIOR_THIGH", "LEFT"]);
  });
});
