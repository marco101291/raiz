import type { Schemas } from "@/lib/api/client";
import styles from "./ReferralBanner.module.css";

const TONE: Record<string, string> = {
  EMERGENCY_NOW: styles.urgencia,
  PROMPT: styles.pronto,
  ROUTINE: styles.rutina,
};

/** The referral: always on top and in its urgency color. It is the only thing that uses those colors. */
export function ReferralBanner({ referral }: { referral?: Schemas["ReferralView"] }) {
  const urgency = referral?.urgency?.id ?? "ROUTINE";
  return (
    <section className={`${styles.banda} ${TONE[urgency] ?? styles.rutina}`} aria-labelledby="derivacion">
      <h1 id="derivacion" className={styles.titulo}>
        {referral?.urgency?.label}
      </h1>
      <p className={styles.mensaje}>{referral?.message}</p>
      {urgency === "EMERGENCY_NOW" && <p className={styles.telefono}>Si no puedes llegar por tu cuenta, llama al 911.</p>}
      <div className={styles.especialistas}>
        <h2>A quién acudir</h2>
        <ul>
          {referral?.specialists?.map((specialist) => <li key={specialist.id}>{specialist.label}</li>)}
        </ul>
      </div>
    </section>
  );
}
