import styles from "./DemoBanner.module.css";

/** Shown on every page until the clinical review is done (docs/revision-clinica.md). Remove it then. */
export function DemoBanner() {
  return (
    <p className={styles.banda} role="note">
      <strong>Demo en desarrollo:</strong> el contenido clínico aún no ha sido revisado por profesionales. No lo uses
      para decidir sobre tu salud.
    </p>
  );
}
