"use client";

import { useState } from "react";
import type { Schemas } from "@/lib/api/client";
import { BodyFigure } from "./body-map/BodyFigure";
import styles from "./body-map/BodyMap.module.css";
import { ZoneList } from "./body-map/ZoneList";
import step from "./step.module.css";
import mapStyles from "./MapStep.module.css";

interface Props {
  zones: Schemas["BodyZoneView"][];
  /** "AXIAL_ONLY" = el dolor no se irradia: el mapa es opcional (README, Formularios). */
  distribution?: string | null;
  defaultValue: string[];
  onChange: (zones: string[]) => void;
  onBack: () => void;
  onSubmit: (zones: string[]) => void;
}

/** Paso 3. Zonas con su lado ("GREAT_TOE-RIGHT"). Un solo campo: no hace falta React Hook Form. */
export function MapStep({ zones, distribution, defaultValue, onChange, onBack, onSubmit }: Props) {
  const [marked, setMarked] = useState<string[]>(defaultValue);
  const [error, setError] = useState(false);
  const optional = distribution === "AXIAL_ONLY";
  const selected = new Set(marked);
  const labels = Object.fromEntries(zones.map((z) => [z.id ?? "", z.label ?? ""]));

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
          Toca las zonas de brazos y piernas donde lo sientes.
          {optional && " Como el dolor no se irradia, este paso es opcional."}
        </p>
        {/* El glúteo no es una BodyZone: no orienta a ninguna raíz y ya se pregunta en el paso del dolor. */}
        <p className={step.intro}>
          ¿Te duele la espalda, el cuello o la nalga? Eso ya lo contaste en el paso anterior; aquí marca
          hasta dónde baja el dolor.
        </p>
      </header>

      <div className={styles.figuras}>
        <BodyFigure view="front" selected={selected} labels={labels} onToggle={toggle} />
        <BodyFigure view="back" selected={selected} labels={labels} onToggle={toggle} />
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
        <ZoneList zones={zones} selected={selected} onToggle={toggle} />
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

/** "GREAT_TOE-RIGHT" → ["GREAT_TOE", "RIGHT"]. Las zonas usan "_", así que el último "-" separa el lado. */
export function splitKey(key: string): [string, "RIGHT" | "LEFT"] {
  const i = key.lastIndexOf("-");
  return [key.slice(0, i), key.slice(i + 1) as "RIGHT" | "LEFT"];
}
