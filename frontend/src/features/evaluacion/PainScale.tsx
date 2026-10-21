import styles from "./PainScale.module.css";

const VALUES = Array.from({ length: 11 }, (_, i) => i);

/** Usual bands of the numeric rating scale (NRS): mild 1–3, moderate 4–6, severe 7–10. */
export function describeIntensity(value: number): string {
  if (value === 0) return "Sin dolor";
  if (value <= 3) return "Leve";
  if (value <= 6) return "Moderado";
  if (value <= 9) return "Intenso";
  return "El peor dolor imaginable";
}

interface Props {
  name: string;
  value: number | null;
  onChange: (value: number) => void;
  describedBy?: string;
  invalid?: boolean;
}

/**
 * 0–10 scale as eleven options in a row that fill up to the chosen number. Unlike a range input, it
 * starts with no value: that way we know the person chose, rather than leaving the control where it was.
 */
export function PainScale({ name, value, onChange, describedBy, invalid }: Props) {
  return (
    <div className={styles.escala}>
      <p className={styles.lectura} aria-live="polite">
        <span className={styles.numero}>{value ?? "–"}</span>
        <span className={styles.descriptor}>{value === null ? "Elige un número" : describeIntensity(value)}</span>
      </p>
      <div className={styles.opciones} role="radiogroup" aria-describedby={describedBy} aria-invalid={invalid || undefined}>
        {VALUES.map((n) => (
          <label key={n} className={value !== null && n <= value ? styles.llena : undefined}>
            <input
              type="radio"
              name={name}
              value={n}
              checked={value === n}
              onChange={() => onChange(n)}
              aria-label={`${n}: ${describeIntensity(n)}`}
            />
            <span aria-hidden="true">{n}</span>
          </label>
        ))}
      </div>
      <p className={styles.extremos} aria-hidden="true">
        <span>Sin dolor</span>
        <span>El peor imaginable</span>
      </p>
    </div>
  );
}
