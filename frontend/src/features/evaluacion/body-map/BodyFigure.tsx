import { useId } from "react";
import { DETAILS, MIDLINE, SCREEN_LEFT_SIDE, SILHOUETTE, ZONES, zoneKey, type View } from "./shapes";
import styles from "./BodyMap.module.css";

const MIRROR = "matrix(-1 0 0 1 220 0)";
const OTHER = { RIGHT: "LEFT", LEFT: "RIGHT" } as const;

interface Props {
  view: View;
  selected: ReadonlySet<string>;
  labels: Record<string, string>;
  onToggle: (key: string) => void;
}

/**
 * Una vista del cuerpo. Es solo para puntero (ratón o dedo): el control accesible es la lista de
 * casillas que va debajo y comparte el mismo estado, así que el SVG queda fuera del árbol de accesibilidad.
 */
export function BodyFigure({ view, selected, labels, onToggle }: Props) {
  const halves = [
    { side: SCREEN_LEFT_SIDE[view], transform: undefined },
    { side: OTHER[SCREEN_LEFT_SIDE[view]], transform: MIRROR },
  ];
  const sideLabel = { RIGHT: "Derecha", LEFT: "Izquierda" } as const;

  // El id del recorte debe ser único en la página: las dos vistas se muestran a la vez.
  const clipId = useId();

  return (
    <figure className={styles.figura}>
      <svg viewBox="0 0 220 516" aria-hidden="true" focusable="false" className={styles.svg}>
        <defs>
          <clipPath id={clipId}>
            <path d={SILHOUETTE[view]} />
          </clipPath>
        </defs>
        {/* Capas: piel, relieves, zonas recortadas a la piel y, encima, el contorno. */}
        {halves.map(({ side, transform }) => (
          <g key={side} transform={transform} className={styles.piel}>
            <path d={SILHOUETTE[view]} />
          </g>
        ))}
        <g className={styles.relieve}>
          {halves.map(({ side, transform }) => (
            <g key={side} transform={transform}>
              {DETAILS[view].map((d) => (
                <path key={d} d={d} />
              ))}
            </g>
          ))}
          {MIDLINE[view].map((d) => (
            <path key={d} d={d} />
          ))}
        </g>
        {/* El recorte va en el mismo grupo que el reflejo: así la silueta se refleja junto con las zonas. */}
        {halves.map(({ side, transform }) => (
          <g key={side} transform={transform} clipPath={`url(#${clipId})`}>
            {ZONES[view].map(({ zone, d }) => {
              const key = zoneKey(zone, side);
              return (
                <path
                  key={key}
                  id={key}
                  d={d}
                  data-zone={key}
                  className={selected.has(key) ? `${styles.zona} ${styles.marcada}` : styles.zona}
                  onClick={() => onToggle(key)}
                >
                  <title>{`${labels[zone] ?? zone}, ${sideLabel[side].toLowerCase()}`}</title>
                </path>
              );
            })}
          </g>
        ))}
        {halves.map(({ side, transform }) => (
          <path key={side} d={SILHOUETTE[view]} transform={transform} className={styles.contorno} />
        ))}
        {/* Rótulos de lado, junto a la cabeza: de frente, la derecha del paciente queda a la izquierda. */}
        <text x="4" y="22" className={styles.lado}>
          {sideLabel[halves[0].side]}
        </text>
        <text x="216" y="22" textAnchor="end" className={styles.lado}>
          {sideLabel[halves[1].side]}
        </text>
      </svg>
      <figcaption>{view === "front" ? "De frente" : "De espalda"}</figcaption>
    </figure>
  );
}
