import type { Schemas } from "@/lib/api/client";
import { zoneKey } from "./shapes";
import styles from "./ZoneList.module.css";

const GROUPS = [
  { region: "CERVICAL", title: "Brazo y mano" },
  { region: "LUMBOSACRAL", title: "Pierna y pie" },
] as const;

interface Props {
  zones: Schemas["BodyZoneView"][];
  selected: ReadonlySet<string>;
  onToggle: (key: string) => void;
}

/**
 * Alternativa accesible obligatoria (README): la misma selección como casillas agrupadas por región.
 * En el móvil es también la forma cómoda de elegir zonas pequeñas como los dedos.
 */
export function ZoneList({ zones, selected, onToggle }: Props) {
  return (
    <div className={styles.grupos}>
      {GROUPS.map(({ region, title }) => (
        <table key={region} className={styles.tabla}>
          <caption>{title}</caption>
          <thead>
            <tr>
              <th scope="col">
                <span className="visually-hidden">Zona</span>
              </th>
              <th scope="col">Derecha</th>
              <th scope="col">Izquierda</th>
            </tr>
          </thead>
          <tbody>
            {zones
              .filter((zone) => zone.region?.id === region)
              .map(({ id = "", label }) => (
                <tr key={id}>
                  <th scope="row">{label}</th>
                  {(["RIGHT", "LEFT"] as const).map((side) => {
                    const key = zoneKey(id, side);
                    return (
                      <td key={side}>
                        <input
                          type="checkbox"
                          checked={selected.has(key)}
                          onChange={() => onToggle(key)}
                          aria-label={`${label}, ${side === "RIGHT" ? "derecha" : "izquierda"}`}
                        />
                      </td>
                    );
                  })}
                </tr>
              ))}
          </tbody>
        </table>
      ))}
    </div>
  );
}
