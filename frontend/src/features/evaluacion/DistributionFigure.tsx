/**
 * Schematic silhouette for "¿Hasta dónde llega el dolor?". The painful stretch is painted in nerve yellow
 * over an ink stroke, on one side of the body: back or neck, then thigh and arm, then below the knee and
 * the elbow.
 */
const BODY = [
  "M18 26 L10 52 L8 74", // left arm
  "M42 26 L50 52 L52 74", // right arm
  "M25 62 L24 90 L23 116", // left leg
  "M35 62 L36 90 L37 116", // right leg
];

const REACH: Record<string, string[]> = {
  AXIAL_ONLY: ["M30 22 L30 30", "M30 46 L30 60"],
  PROXIMAL: ["M30 22 L30 30", "M30 46 L30 60", "M42 26 L50 52", "M35 62 L36 90"],
  DISTAL: ["M30 22 L30 30", "M30 46 L30 60", "M42 26 L50 52 L52 74", "M35 62 L36 90 L37 116"],
};

export function DistributionFigure({ distribution }: { distribution: string }) {
  const reach = REACH[distribution] ?? [];
  return (
    <svg viewBox="0 0 60 122" width="56" height="114" aria-hidden="true" focusable="false">
      <circle cx="30" cy="11" r="8" fill="var(--hueso-hondo)" stroke="var(--linea)" strokeWidth="2" />
      <rect x="18" y="21" width="24" height="42" rx="9" fill="var(--hueso-hondo)" stroke="var(--linea)" strokeWidth="2" />
      {BODY.map((d) => (
        <path key={d} d={d} fill="none" stroke="var(--linea)" strokeWidth="6" strokeLinecap="round" strokeLinejoin="round" />
      ))}
      {reach.map((d) => (
        <g key={d} fill="none" strokeLinecap="round" strokeLinejoin="round">
          <path d={d} stroke="var(--tinta)" strokeWidth="6.5" />
          <path d={d} stroke="var(--nervio)" strokeWidth="3.5" />
        </g>
      ))}
    </svg>
  );
}
