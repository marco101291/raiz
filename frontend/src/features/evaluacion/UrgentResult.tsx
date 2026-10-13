import type { Schemas } from "@/lib/api/client";
import styles from "./UrgentResult.module.css";

interface Props {
  result: Schemas["UrgentReferral"];
  onEdit: () => void;
}

/**
 * Hubo alarma: la pantalla muestra solo la derivación y los datos de alarma (README, paso 5).
 * Sin patrón de dolor, sin raíces y sin rehabilitación.
 */
export function UrgentResult({ result, onEdit }: Props) {
  const { referral, reported = [], inferred = [] } = result;
  const emergency = referral?.urgency?.id === "EMERGENCY_NOW";

  return (
    <article className={styles.resultado}>
      <section className={emergency ? styles.bandaUrgencia : styles.bandaPronto} aria-labelledby="derivacion">
        <h1 id="derivacion" className={styles.urgencia}>
          {referral?.urgency?.label}
        </h1>
        <p className={styles.mensaje}>{referral?.message}</p>
        {emergency && <p className={styles.telefono}>Si no puedes llegar por tu cuenta, llama al 911.</p>}
      </section>

      <section className={styles.seccion} aria-labelledby="quien">
        <h2 id="quien">A quién acudir</h2>
        <ul>
          {referral?.specialists?.map((specialist) => <li key={specialist.id}>{specialist.label}</li>)}
        </ul>
      </section>

      <section className={styles.seccion} aria-labelledby="por-que">
        <h2 id="por-que">Por lo que respondiste</h2>
        <ul>
          {reported.map((flag) => <li key={flag.id}>{flag.label}</li>)}
          {inferred.map((flag) => (
            <li key={flag.id}>
              {flag.label}. Lo detectamos porque marcaste síntomas en ambos lados del cuerpo.
            </li>
          ))}
        </ul>
      </section>

      <p className={styles.aviso}>{referral?.disclaimer}</p>

      <button type="button" className={styles.editar} onClick={onEdit}>
        Cambiar mis respuestas
      </button>
    </article>
  );
}
