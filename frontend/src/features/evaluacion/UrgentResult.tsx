import type { Schemas } from "@/lib/api/client";
import { ReferralBanner } from "./ReferralBanner";
import result from "./result.module.css";

/**
 * There was a red flag: the screen shows only the referral and the red flags (README, step 5).
 * No pain pattern, no roots and no rehab.
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
