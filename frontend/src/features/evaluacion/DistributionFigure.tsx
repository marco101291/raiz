/**
 * Silueta esquemática para "¿Hasta dónde llega el dolor?". El tramo con dolor se pinta en amarillo
 * nervio sobre trazo de tinta, sobre un lado del cuerpo: espalda o cuello, luego muslo y brazo, luego
 * por debajo de la rodilla y el codo.
 */
const BODY = [
  "M18 26 L10 52 L8 74", // brazo izquierdo
  "M42 26 L50 52 L52 74", // brazo derecho
  "M25 62 L24 90 L23 116", // pierna izquierda
  "M35 62 L36 90 L37 116", // pierna derecha
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
