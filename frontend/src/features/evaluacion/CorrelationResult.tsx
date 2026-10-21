import type { Schemas } from "@/lib/api/client";
import { ReferralBanner } from "./ReferralBanner";
import result from "./result.module.css";

type Assessment = Schemas["RootCorrelation"] | Schemas["NoRootCorrelation"];

/**
 * Sin alarma: derivación, lectura del patrón de dolor y, si las hay, raíces compatibles con su
 * evidencia. Siempre "compatible con", nunca "tienes" (README, principio 3).
 */
export function CorrelationResult({ result: assessment }: { result: Assessment }) {
  const { referral, pattern } = assessment;
  const matches = assessment.type === "ROOT_CORRELATION" ? (assessment.matches ?? []) : [];
  const note = assessment.type === "NO_ROOT_CORRELATION" ? assessment.note : undefined;
  const otherCauses = assessment.otherCauses ?? [];

  return (
    <>
      <ReferralBanner referral={referral} />

      {pattern?.type !== "NOT_ASSESSED" && (
        <section className={result.seccion} aria-labelledby="patron">
          <h2 id="patron">Cómo se comporta tu dolor</h2>
          <p>{pattern?.explanation}</p>
          {pattern?.features && pattern.features.length > 0 && (
            <ul className={result.lista}>
              {pattern.features.map((feature) => <li key={feature}>{feature}</li>)}
            </ul>
          )}
          {pattern?.chronicity && <p className={result.dato}>{pattern.chronicity.label}</p>}
        </section>
      )}

      {matches.length > 0 && (
        <section className={result.seccion} aria-labelledby="raices">
          <h2 id="raices">Raíces compatibles con tus síntomas</h2>
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
        </section>
      )}

      {otherCauses.length > 0 && (
        <section className={result.seccion} aria-labelledby="otras-causas">
          <h2 id="otras-causas">También puede deberse a</h2>
          <p className={result.ayuda}>
            Lo que marcaste no siempre viene de una raíz nerviosa. Estas causas son frecuentes en esas zonas
            y un profesional puede distinguirlas.
          </p>
          <ul className={result.raices}>
            {otherCauses.map((cause) => (
              <li key={cause.id} className={`${result.raiz} ${result.otraCausa}`}>
                <h3>{cause.title}</h3>
                <p>{cause.description}</p>
                <p className={result.ayuda}>Fuente: {cause.sourceReference}</p>
              </li>
            ))}
          </ul>
        </section>
      )}

      {note && (
        <section className={result.seccion} aria-labelledby="nota">
          <h2 id="nota">Qué significa</h2>
          <p>{note}</p>
        </section>
      )}
    </>
  );
}
