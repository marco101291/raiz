import styles from "./Progress.module.css";

const STEPS = [
  { id: "alarma", label: "Señales de alarma" },
  { id: "dolor", label: "Tu dolor" },
  { id: "mapa", label: "Mapa del cuerpo" },
  { id: "fuerza", label: "Fuerza" },
  { id: "resultado", label: "Resultado" },
] as const;

/** Segmented progress bar, like vertebrae. The steps really are a sequence. */
export function Progress({ current }: { current: (typeof STEPS)[number]["id"] }) {
  const index = STEPS.findIndex((step) => step.id === current);
  return (
    <nav aria-label="Progreso de la evaluación" className={styles.progress}>
      <p className={styles.texto}>
        Paso {index + 1} de {STEPS.length}: <strong>{STEPS[index].label}</strong>
      </p>
      <ol className={styles.segmentos}>
        {STEPS.map((step, i) => (
          <li
            key={step.id}
            className={i <= index ? styles.hecho : undefined}
            aria-current={i === index ? "step" : undefined}
          >
            <span className="visually-hidden">{step.label}</span>
          </li>
        ))}
      </ol>
    </nav>
  );
}
