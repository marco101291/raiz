import type { Schemas } from "@/lib/api/client";
import { ReferralBanner } from "./ReferralBanner";
import result from "./result.module.css";

/**
 * Hubo alarma: la pantalla muestra solo la derivación y los datos de alarma (README, paso 5).
 * Sin patrón de dolor, sin raíces y sin rehabilitación.
 */
export function UrgentResult({ result: assessment }: { result: Schemas["UrgentReferral"] }) {
  const { referral, reported = [], inferred = [] } = assessment;
  return (
    <>
      <ReferralBanner referral={referral} />
      <section className={result.seccion} aria-labelledby="por-que">
        <h2 id="por-que">Por lo que respondiste</h2>
        <ul className={result.lista}>
          {reported.map((flag) => <li key={flag.id}>{flag.label}</li>)}
          {inferred.map((flag) => (
            <li key={flag.id}>{flag.label}. Lo detectamos porque marcaste síntomas en ambos lados del cuerpo.</li>
          ))}
        </ul>
      </section>
    </>
  );
}
