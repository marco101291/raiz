/**
 * Geometría del mapa corporal (viewBox 0 0 220 516), al estilo de una lámina de dermatomas: silueta
 * anatómica de proporciones reales (canon de 8 cabezas) con las zonas recortadas dentro de la piel.
 *
 * Todo se describe para la mitad que queda a la IZQUIERDA de la pantalla; la otra se obtiene
 * reflejando en x = 110. De frente, la izquierda de la pantalla es el lado derecho del paciente;
 * de espalda, el izquierdo.
 *
 * Cada BodyZone aparece en una sola vista para que no haya dos controles para la misma zona.
 * Las manos van en posición anatómica (palmas al frente): el pulgar queda hacia afuera.
 */
export type View = "front" | "back";

export interface ZoneShape {
  zone: string;
  d: string;
}

// Contorno de media figura, de la coronilla a la entrepierna. Es un trazo abierto: el relleno lo
// cierra solo por la línea media (x = 110) y el trazo no la dibuja, así las dos mitades se unen sin costura.
const HEAD_AND_ARM =
  "M110 8 C98 8 91 17 91 31 C90 34 87 34 87 39 C87 44 88 48 91 48 C92 54 95 58 99 61 L100 70 " + // cabeza y cuello
  "C99 76 90 78 78 81 C68 83 59 88 57 100 " + // trapecio y hombro
  "C55 112 51 140 48 168 C47 176 45 182 43 190 C40 202 38 220 36 234 " + // borde externo del brazo
  "C34 240 31 244 30 250 C28 256 25 262 24.5 267 C24 271 27 272 28.5 268 L32.6 258 " + // pulgar
  "L33.2 263 L33.2 279 Q35.1 284.5 37 279 L37.2 264 " + // índice
  "L37.6 264 L37.6 282 Q39.5 287.5 41.4 282 L41.5 264 " + // medio
  "L41.8 263 L41.8 280 Q43.6 285.5 45.4 280 L45.5 262 " + // anular
  "L45.8 261 L45.8 275 Q47.4 279.5 49 275 L49.4 260 " + // meñique
  "C50.4 252 50 244 48.5 234 C50 220 56 200 59 188 C61 182 61 178 62 172 " + // borde interno del antebrazo
  "C64 150 67 130 71 118 C72 115 74 114 76 115 " + // borde interno del brazo hasta la axila
  "C78 125 79 140 81 160 C82 172 82 182 80 192 C77 202 73 214 72 228 " + // tronco y cadera
  "C71 250 74 290 78 326 C79 336 80 346 79 356 C76 372 76 392 82 414 C85 424 86 428 86 432 "; // pierna, cara externa

const LEG_INNER =
  "C106 436 106 430 103 426 C103 410 106 395 106 380 C106 368 104 360 105 352 " + // maléolo interno y pantorrilla
  "C107 344 107 336 106 326 C107 300 109 260 110 236"; // rodilla y muslo, cara interna

// De frente se ven el empeine y los dedos; de espalda, el talón.
const FOOT_FRONT =
  "C85.5 435 85 437 85 439 C83 444 80 450 80 454 " + // maléolo externo y borde externo del pie
  "C79.5 457 82 458.5 83 457 C83.5 459.5 86.5 460 87 458 C87.5 461 91 461.5 91.5 459 " + // dedos 5, 4 y 3
  "C92 462.5 96 463 96.5 460 C97 465 105 465.5 106 460 C106.5 455 105.5 448 104 440 "; // dedo 2, dedo gordo
const FOOT_BACK = "C85 438 84 444 85 449 C86 455 92 457 97 457 C102 457 105 453 105 448 C105 445 104.5 442 104 440 ";

// La planta no se ve en un dibujo de espalda: va aparte, bajo cada talón, con los dedos hacia abajo.
const SOLE =
  "M95 467 C99.5 467 102 470.5 101.5 476 C101 483 99.5 488 102 494 C104 498 104 501 102 502.5 " +
  "C99 504 90 503 87.5 501 C85.5 499 86.5 490 88 481 C88.5 472 91 467 95 467 Z " +
  "M97.3 507.5 a3.2 4 0 1 0 6.4 0 a3.2 4 0 1 0 -6.4 0 Z " + // dedo gordo
  "M93.4 507 a1.9 2.7 0 1 0 3.8 0 a1.9 2.7 0 1 0 -3.8 0 Z " +
  "M89.9 506.3 a1.7 2.4 0 1 0 3.4 0 a1.7 2.4 0 1 0 -3.4 0 Z " +
  "M86.9 505.2 a1.5 2.1 0 1 0 3 0 a1.5 2.1 0 1 0 -3 0 Z " +
  "M84.5 503.6 a1.3 1.8 0 1 0 2.6 0 a1.3 1.8 0 1 0 -2.6 0 Z";

/** Silueta de media figura: se usa como relleno, como contorno y como recorte de las zonas. */
export const SILHOUETTE: Record<View, string> = {
  front: HEAD_AND_ARM + FOOT_FRONT + LEG_INNER,
  back: HEAD_AND_ARM + FOOT_BACK + LEG_INNER + " " + SOLE,
};

/** Relieves anatómicos de referencia (no se pueden marcar). Se reflejan con su mitad. */
export const DETAILS: Record<View, string[]> = {
  front: [
    "M101 80 C95 84 86 84 74 86", // clavícula
    "M77 87 C74 96 73 106 74 113", // pliegue axilar anterior
    "M78 122 C86 132 98 134 108 128", // borde inferior del pectoral
    "M84 196 C88 210 96 222 106 230", // pliegue inguinal (bajo la zona de la ingle)
    "M48 184 Q54 187 60 184", // pliegue del codo
    "M37 236 Q42 238 48 236", // pliegue de la muñeca
    "M84 340 C84 332 96 332 96 340 C96 348 84 348 84 340 Z", // rótula
    "M89 362 C90 380 91 400 94 418", // cresta de la tibia
  ],
  back: [
    "M96 94 C94 110 94 126 97 140 C92 134 86 128 82 120", // escápula
    "M76 196 C86 202 98 204 108 204", // cresta ilíaca
    "M50 184 Q54 180 58 184", // olécranon
    "M86 344 Q95 347 104 344", // hueco poplíteo
    "M96 420 C97 428 98 434 98 442", // tendón de Aquiles
  ],
};

/** Relieves sobre la línea media: se dibujan una sola vez, sin reflejar. */
export const MIDLINE: Record<View, string[]> = {
  front: [
    "M110 178 c-2 0 -2.5 3 0 4.5 c2.5 -1.5 2 -4.5 0 -4.5 Z", // ombligo
    "M110 92 L110 150", // esternón
  ],
  back: [
    "M110 74 L110 200", // columna
    "M110 208 L110 236", // pliegue interglúteo
  ],
};

// Las zonas pueden sobresalir de la piel: el recorte con la silueta les da el borde anatómico.
export const ZONES: Record<View, ZoneShape[]> = {
  front: [
    // Miembro superior
    { zone: "LATERAL_SHOULDER", d: "M76 80 L36 80 L30 180 L54 180 L66 114 L73 100 Z" },
    { zone: "MEDIAL_ARM", d: "M73 100 L66 114 L54 180 L64 180 L74 114 Z" },
    { zone: "LATERAL_FOREARM", d: "M28 190 L53 190 L42 234 L28 234 Z" },
    { zone: "MEDIAL_FOREARM", d: "M53 190 L70 190 L56 234 L42 234 Z" },
    { zone: "THUMB", d: "M34 247 L20 250 L18 274 L30 274 L32.9 258 Z" },
    { zone: "INDEX_FINGER", d: "M32 262 H37.4 V290 H32 Z" },
    { zone: "MIDDLE_FINGER", d: "M37.4 263 H41.6 V290 H37.4 Z" },
    { zone: "RING_FINGER", d: "M41.6 262 H45.6 V290 H41.6 Z" },
    { zone: "LITTLE_FINGER", d: "M45.6 261 H52 V290 H45.6 Z" },
    // Miembro inferior
    { zone: "GROIN", d: "M83 196 L112 224 L112 236 L78 212 Z" },
    { zone: "UPPER_ANTERIOR_THIGH", d: "M78 212 L112 236 L112 280 L64 280 L64 214 Z" },
    { zone: "ANTERIOR_THIGH", d: "M64 280 H112 V326 H64 Z" },
    { zone: "MEDIAL_KNEE", d: "M97 326 H112 V358 H97 Z" },
    { zone: "LATERAL_LEG", d: "M64 358 L93 358 L96 424 L64 424 Z" },
    { zone: "MEDIAL_LEG", d: "M93 358 L112 358 L112 424 L96 424 Z" },
    { zone: "MEDIAL_MALLEOLUS", d: "M97 431 a4.5 4.5 0 1 0 9 0 a4.5 4.5 0 1 0 -9 0 Z" },
    { zone: "DORSUM_FOOT", d: "M86 439 L104 439 L104 450 L96.8 452 L96.8 454 L86 454 Z" },
    { zone: "GREAT_TOE", d: "M96.8 452 L104 450 L112 450 L112 470 L96.8 470 Z" },
    { zone: "LATERAL_FOOT", d: "M70 440 L86 439 L86 456 L70 458 Z" },
  ],
  back: [
    { zone: "POSTERIOR_THIGH", d: "M62 228 Q88 250 112 238 L112 334 L62 334 Z" },
    { zone: "POSTERIOR_CALF", d: "M62 352 H112 V418 H62 Z" },
    { zone: "SOLE", d: SOLE },
  ],
};

/** Lado del paciente para la mitad de la izquierda de la pantalla, según la vista. */
export const SCREEN_LEFT_SIDE: Record<View, "RIGHT" | "LEFT"> = { front: "RIGHT", back: "LEFT" };

export const zoneKey = (zone: string, side: "RIGHT" | "LEFT") => `${zone}-${side}`;
