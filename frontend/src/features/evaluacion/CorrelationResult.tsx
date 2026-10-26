import type { Schemas } from "@/lib/api/client";
import { ReferralBanner } from "./ReferralBanner";
import result from "./result.module.css";

type Assessment = Schemas["RootCorrelation"] | Schemas["NoRootCorrelation"];

interface Props {
  result: Assessment;
  /** Emergency red flags from the triage catalog: what should send the person to the ER from now on. */
  warningSigns: Schemas["RedFlagView"][];
}

/**
 * No red flag. Ordered by what the person needs: what to do, when to go to the ER, what it may be, and
 * last, folded, the compatible roots, which matter more to the professional than to the patient.
 * Always "compatible with", never "you have" (README, principle 3).
 */
export function CorrelationResult({ result: assessment, warningSigns }: Props) {
  const { referral, pattern } = assessment;
  const matches = assessment.type === "ROOT_CORRELATION" ? (assessment.matches ?? []) : [];
  const note = assessment.type === "NO_ROOT_CORRELATION" ? assessment.note : undefined;
  const otherCauses = assessment.otherCauses ?? [];
  const patternAssessed = pattern?.type !== undefined && pattern.type !== "NOT_ASSESSED";

  return (
    <>
      <ReferralBanner referral={referral} />

      {warningSigns.length > 0 && (
        <section className={`${result.seccion} ${result.alerta}`} aria-labelledby="urgencias">
          <h2 id="urgencias">Cuándo ir a urgencias</h2>
          <p>Si aparece alguno de estos, no esperes a tu cita:</p>
          <ul className={result.lista}>
            {warningSigns.map((sign) => <li key={sign.id}>{sign.label}</li>)}
          </ul>
        </section>
      )}

      {(note || patternAssessed || otherCauses.length > 0) && (
        <section className={result.seccion} aria-labelledby="que-puede-ser">
          <h2 id="que-puede-ser">Qué puede ser</h2>
          {note && <p>{note}</p>}

          {patternAssessed && (
            <div className={result.bloque}>
              <h3>Cómo se comporta tu dolor</h3>
              <p>{pattern?.explanation}</p>
              {pattern?.features && pattern.features.length > 0 && (
                <ul className={result.lista}>
                  {pattern.features.map((feature) => <li key={feature}>{feature}</li>)}
                </ul>
              )}
              {pattern?.chronicity && <p className={result.dato}>{pattern.chronicity.label}</p>}
            </div>
          )}

          {otherCauses.length > 0 && (
            <div className={result.bloque}>
              <h3>Otras causas frecuentes en las zonas que marcaste</h3>
              <p className={result.ayuda}>
                Lo que marcaste no siempre viene de un nervio de la columna. Un profesional puede distinguirlas.
              </p>
              <ul className={result.causas}>
                {otherCauses.map((cause) => (
                  <li key={cause.id} className={result.causa}>
                    <h4>{cause.title}</h4>
                    <p>{cause.description}</p>
                    <p className={result.ayuda}>Fuente: {cause.sourceReference}</p>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </section>
      )}

      {matches.length > 0 && (
        <details className={result.medico}>
          <summary>Para tu médico: raíces nerviosas compatibles</summary>
          <p className={result.ayuda}>
            Los territorios de las raíces se solapan y varían entre personas: esto orienta, no confirma.
          </p>
          <ol className={result.raices}>
            {matches.map((match) => (
              <li key={match.label} className={result.raiz}>
                <h3>{match.label}</h3>
                {match.neurologicalDeficit && (
                  <p className={result.deficit}>Hay datos de pérdida de fuerza o de reflejos.</p>
                )}
                <ul className={result.lista}>
                  {match.evidence?.map((e) => <li key={`${e.kind}-${e.finding}`}>{e.explanation}</li>)}
                </ul>
              </li>
            ))}
          </ol>
        </details>
      )}
    </>
  );
}
