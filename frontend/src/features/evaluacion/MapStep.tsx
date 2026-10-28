"use client";

import { useState } from "react";
import type { Schemas } from "@/lib/api/client";
import { BodyFigure } from "./body-map/BodyFigure";
import { FULL_VIEWBOX, REGION_VIEWBOX, ZONES, type View } from "./body-map/shapes";
import styles from "./body-map/BodyMap.module.css";
import { ZoneList } from "./body-map/ZoneList";
import step from "./step.module.css";
import mapStyles from "./MapStep.module.css";

interface Props {
  zones: Schemas["BodyZoneView"][];
  /** The pain does not radiate from any region: the map is optional (README, Forms). */
  optional?: boolean;
  /** Where the pain starts ("CERVICAL", "LUMBOSACRAL"): only that limb is shown. Empty shows both. */
  regions?: string[];
  defaultValue: string[];
  onChange: (zones: string[]) => void;
  onBack: () => void;
  onSubmit: (zones: string[]) => void;
}

/** Step 3. Zones with their side ("GREAT_TOE-RIGHT"). A single field: no need for React Hook Form. */
const INTRO: Record<string, string> = {
  CERVICAL: "Toca las zonas del brazo y la mano donde lo sientes.",
  LUMBOSACRAL: "Toca las zonas de la pierna y el pie donde lo sientes.",
};

export function MapStep({ zones, optional = false, regions = [], defaultValue, onChange, onBack, onSubmit }: Props) {
  const shown = regions.length > 0 ? zones.filter((zone) => regions.includes(zone.region.id)) : zones;
  const visible = new Set(shown.map((zone) => zone.id));
  // Zones marked before changing where the pain starts are dropped: they could no longer be unmarked.
  const [marked, setMarked] = useState<string[]>(() => defaultValue.filter((key) => visible.has(splitKey(key)[0])));
  const [error, setError] = useState(false);
  const selected = new Set(marked);
  const labels = Object.fromEntries(zones.map((z) => [z.id, z.label]));
  const viewBox = regions.length === 1 ? (REGION_VIEWBOX[regions[0]] ?? FULL_VIEWBOX) : FULL_VIEWBOX;
  const views = (["front", "back"] as View[]).filter((view) => ZONES[view].some(({ zone }) => visible.has(zone)));

  const toggle = (key: string) => {
    const next = selected.has(key) ? marked.filter((k) => k !== key) : [...marked, key];
    setMarked(next);
    setError(false);
    onChange(next);
  };

  const describe = (key: string) => {
    const [zone, side] = splitKey(key);
    return `${labels[zone] ?? zone}, ${side === "RIGHT" ? "derecha" : "izquierda"}`;
  };

  return (
    <form
      className={step.form}
      noValidate
      onSubmit={(event) => {
        event.preventDefault();
        if (!optional && marked.length === 0) {
          setError(true);
          return;
        }
        onSubmit(marked);
      }}
    >
      <header className={step.encabezado}>
        <h1 className={step.titulo}>¿Dónde sientes el dolor o el hormigueo?</h1>
        <p className={step.intro}>
          {regions.length === 1 ? INTRO[regions[0]] : "Toca las zonas de brazos y piernas donde lo sientes."}
          {optional && " Como el dolor no baja al brazo ni a la pierna, este paso es opcional."}
        </p>
        {/* The buttock is not a BodyZone: it points to no root and is already asked in the pain step. */}
        <p className={step.intro}>
          ¿Te duele la espalda, el cuello o el glúteo? Eso ya lo contaste en el paso anterior; aquí marca
          hasta dónde baja el dolor.
        </p>
      </header>

      <div className={styles.figuras}>
        {views.map((view) => (
          <BodyFigure
            key={view}
            view={view}
            visible={visible}
            viewBox={viewBox}
            selected={selected}
            labels={labels}
            onToggle={toggle}
          />
        ))}
      </div>

      <section className={mapStyles.marcadas} aria-labelledby="marcadas" aria-live="polite">
        <h2 id="marcadas" className={mapStyles.subtitulo}>
          {marked.length === 0 ? "Aún no marcas ninguna zona" : `Marcaste ${marked.length} ${marked.length === 1 ? "zona" : "zonas"}`}
        </h2>
        {marked.length > 0 && (
          <ul className={mapStyles.chips}>
            {marked.map((key) => (
              <li key={key}>
                {describe(key)}
                <button type="button" onClick={() => toggle(key)} aria-label={`Quitar ${describe(key)}`}>
                  Quitar
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

      {error && (
        <p role="alert" className={step.resumenError}>
          Marca al menos una zona donde lo sientes, en el dibujo o en la lista.
        </p>
      )}

      <section className={mapStyles.lista} aria-labelledby="lista">
        <h2 id="lista" className={mapStyles.subtitulo}>
          O elígelas de una lista
        </h2>
        <ZoneList zones={shown} selected={selected} onToggle={toggle} />
      </section>

      <div className={step.acciones}>
        <button type="button" className={step.secundario} onClick={onBack}>
          Atrás
        </button>
        <button type="submit" className={step.principal}>
          {optional && marked.length === 0 ? "Continuar sin marcar" : "Continuar"}
        </button>
      </div>
    </form>
  );
}

/** "GREAT_TOE-RIGHT" → ["GREAT_TOE", "RIGHT"]. Zones use "_", so the last "-" separates the side. */
export function splitKey(key: string): [string, "RIGHT" | "LEFT"] {
  const i = key.lastIndexOf("-");
  return [key.slice(0, i), key.slice(i + 1) as "RIGHT" | "LEFT"];
}
