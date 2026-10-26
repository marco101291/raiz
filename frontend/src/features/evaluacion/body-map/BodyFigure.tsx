import { useId } from "react";
import { DETAILS, MIDLINE, SCREEN_LEFT_SIDE, SILHOUETTE, ZONES, zoneKey, type View } from "./shapes";
import styles from "./BodyMap.module.css";

const MIRROR = "matrix(-1 0 0 1 220 0)";
const OTHER = { RIGHT: "LEFT", LEFT: "RIGHT" } as const;

interface Props {
  view: View;
  /** Zones that can be marked; the others are drawn as plain skin. */
  visible: ReadonlySet<string>;
  viewBox: string;
  selected: ReadonlySet<string>;
  labels: Record<string, string>;
  onToggle: (key: string) => void;
}

/**
 * One view of the body. It is pointer-only (mouse or finger): the accessible control is the checkbox list
 * below, which shares the same state, so the SVG stays out of the accessibility tree.
 */
export function BodyFigure({ view, visible, viewBox, selected, labels, onToggle }: Props) {
  const halves = [
    { side: SCREEN_LEFT_SIDE[view], transform: undefined },
    { side: OTHER[SCREEN_LEFT_SIDE[view]], transform: MIRROR },
  ];
  const sideLabel = { RIGHT: "Derecha", LEFT: "Izquierda" } as const;

  // The clip id must be unique on the page: both views are shown at once.
  const clipId = useId();

  return (
    <figure className={styles.figura}>
      {/* Side labels in HTML, above the drawing: inside the SVG they would collide with the limb once cropped.
          In the front view, the patient's right is on the left. */}
      <div className={styles.lados} aria-hidden="true">
        <span>{sideLabel[halves[0].side]}</span>
        <span>{sideLabel[halves[1].side]}</span>
      </div>
      <svg viewBox={viewBox} aria-hidden="true" focusable="false" className={styles.svg}>
        <defs>
          <clipPath id={clipId}>
            <path d={SILHOUETTE[view]} />
          </clipPath>
        </defs>
        {/* Layers: skin, details, zones clipped to the skin and, on top, the outline. */}
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
        {/* The clip is in the same group as the mirroring: that way the silhouette mirrors along with the zones. */}
        {halves.map(({ side, transform }) => (
          <g key={side} transform={transform} clipPath={`url(#${clipId})`}>
            {ZONES[view]
              .filter(({ zone }) => visible.has(zone))
              .map(({ zone, d }) => {
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
      </svg>
      <figcaption>{view === "front" ? "De frente" : "De espalda"}</figcaption>
    </figure>
  );
}
