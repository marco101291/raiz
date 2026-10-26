/**
 * Body map geometry (viewBox 0 0 220 516), in the style of a dermatome chart: an anatomical silhouette with
 * real proportions (8-head canon) with the zones clipped inside the skin.
 *
 * Everything is described for the half on the LEFT of the screen; the other is obtained by mirroring at
 * x = 110. In the front view, the left of the screen is the patient's right side; in the back view, their
 * left.
 *
 * Each BodyZone appears in a single view so there are never two controls for the same zone.
 * The hands are in anatomical position (palms forward): the thumb points outward.
 */
export type View = "front" | "back";

export interface ZoneShape {
  zone: string;
  d: string;
}

// Outline of half a figure, from the crown to the crotch. It is an open path: the fill closes it along the
// midline (x = 110) and the stroke does not draw it, so the two halves join without a seam.
const HEAD_AND_ARM =
  "M110 8 C98 8 91 17 91 31 C90 34 87 34 87 39 C87 44 88 48 91 48 C92 54 95 58 99 61 L100 70 " + // head and neck
  "C99 76 90 78 78 81 C68 83 59 88 57 100 " + // trapezius and shoulder
  "C55 112 51 140 48 168 C47 176 45 182 43 190 C40 202 38 220 36 234 " + // outer edge of the arm
  "C34 240 31 244 30 250 C28 256 25 262 24.5 267 C24 271 27 272 28.5 268 L32.6 258 " + // thumb
  "L33.2 263 L33.2 279 Q35.1 284.5 37 279 L37.2 264 " + // index
  "L37.6 264 L37.6 282 Q39.5 287.5 41.4 282 L41.5 264 " + // middle
  "L41.8 263 L41.8 280 Q43.6 285.5 45.4 280 L45.5 262 " + // ring
  "L45.8 261 L45.8 275 Q47.4 279.5 49 275 L49.4 260 " + // little
  "C50.4 252 50 244 48.5 234 C50 220 56 200 59 188 C61 182 61 178 62 172 " + // inner edge of the forearm
  "C64 150 67 130 71 118 C72 115 74 114 76 115 " + // inner edge of the arm up to the armpit
  "C78 125 79 140 81 160 C82 172 82 182 80 192 C77 202 73 214 72 228 " + // trunk and hip
  "C71 250 74 290 78 326 C79 336 80 346 79 356 C76 372 76 392 82 414 C85 424 86 428 86 432 "; // leg, outer side

const LEG_INNER =
  "C106 436 106 430 103 426 C103 410 106 395 106 380 C106 368 104 360 105 352 " + // medial malleolus and calf
  "C107 344 107 336 106 326 C107 300 109 260 110 236"; // knee and thigh, inner side

// From the front the instep and toes show; from the back, the heel.
const FOOT_FRONT =
  "C85.5 435 85 437 85 439 C83 444 80 450 80 454 " + // lateral malleolus and outer edge of the foot
  "C79.5 457 82 458.5 83 457 C83.5 459.5 86.5 460 87 458 C87.5 461 91 461.5 91.5 459 " + // toes 5, 4 and 3
  "C92 462.5 96 463 96.5 460 C97 465 105 465.5 106 460 C106.5 455 105.5 448 104 440 "; // toe 2, big toe
const FOOT_BACK = "C85 438 84 444 85 449 C86 455 92 457 97 457 C102 457 105 453 105 448 C105 445 104.5 442 104 440 ";

// The sole is not visible in a back view: it goes separately, under each heel, toes pointing down.
const SOLE =
  "M95 467 C99.5 467 102 470.5 101.5 476 C101 483 99.5 488 102 494 C104 498 104 501 102 502.5 " +
  "C99 504 90 503 87.5 501 C85.5 499 86.5 490 88 481 C88.5 472 91 467 95 467 Z " +
  "M97.3 507.5 a3.2 4 0 1 0 6.4 0 a3.2 4 0 1 0 -6.4 0 Z " + // big toe
  "M93.4 507 a1.9 2.7 0 1 0 3.8 0 a1.9 2.7 0 1 0 -3.8 0 Z " +
  "M89.9 506.3 a1.7 2.4 0 1 0 3.4 0 a1.7 2.4 0 1 0 -3.4 0 Z " +
  "M86.9 505.2 a1.5 2.1 0 1 0 3 0 a1.5 2.1 0 1 0 -3 0 Z " +
  "M84.5 503.6 a1.3 1.8 0 1 0 2.6 0 a1.3 1.8 0 1 0 -2.6 0 Z";

/** Half-figure silhouette: used as fill, as outline and as the clip for the zones. */
export const SILHOUETTE: Record<View, string> = {
  front: HEAD_AND_ARM + FOOT_FRONT + LEG_INNER,
  back: HEAD_AND_ARM + FOOT_BACK + LEG_INNER + " " + SOLE,
};

/** Anatomical reference details (not selectable). They are mirrored with their half. */
export const DETAILS: Record<View, string[]> = {
  front: [
    "M101 80 C95 84 86 84 74 86", // clavicle
    "M77 87 C74 96 73 106 74 113", // anterior axillary fold
    "M78 122 C86 132 98 134 108 128", // lower edge of the pectoral
    "M84 196 C88 210 96 222 106 230", // inguinal fold (under the groin zone)
    "M48 184 Q54 187 60 184", // elbow crease
    "M37 236 Q42 238 48 236", // wrist crease
    "M84 340 C84 332 96 332 96 340 C96 348 84 348 84 340 Z", // kneecap
    "M89 362 C90 380 91 400 94 418", // tibial crest
  ],
  back: [
    "M96 94 C94 110 94 126 97 140 C92 134 86 128 82 120", // scapula
    "M76 196 C86 202 98 204 108 204", // iliac crest
    "M50 184 Q54 180 58 184", // olecranon
    "M86 344 Q95 347 104 344", // popliteal fossa
    "M96 420 C97 428 98 434 98 442", // Achilles tendon
  ],
};

/** Details on the midline: drawn once, without mirroring. */
export const MIDLINE: Record<View, string[]> = {
  front: [
    "M110 178 c-2 0 -2.5 3 0 4.5 c2.5 -1.5 2 -4.5 0 -4.5 Z", // navel
    "M110 92 L110 150", // sternum
  ],
  back: [
    "M110 74 L110 200", // spine
    "M110 208 L110 236", // gluteal cleft
  ],
};

// Zones may stick out of the skin: clipping with the silhouette gives them the anatomical edge.
export const ZONES: Record<View, ZoneShape[]> = {
  front: [
    // Upper limb
    { zone: "LATERAL_SHOULDER", d: "M76 80 L36 80 L30 180 L54 180 L66 114 L73 100 Z" },
    { zone: "MEDIAL_ARM", d: "M73 100 L66 114 L54 180 L64 180 L74 114 Z" },
    { zone: "LATERAL_FOREARM", d: "M28 190 L53 190 L42 234 L28 234 Z" },
    { zone: "MEDIAL_FOREARM", d: "M53 190 L70 190 L56 234 L42 234 Z" },
    { zone: "THUMB", d: "M34 247 L20 250 L18 274 L30 274 L32.9 258 Z" },
    { zone: "INDEX_FINGER", d: "M32 262 H37.4 V290 H32 Z" },
    { zone: "MIDDLE_FINGER", d: "M37.4 263 H41.6 V290 H37.4 Z" },
    { zone: "RING_FINGER", d: "M41.6 262 H45.6 V290 H41.6 Z" },
    { zone: "LITTLE_FINGER", d: "M45.6 261 H52 V290 H45.6 Z" },
    // Lower limb
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

/** Full drawing, and the crop for each region so the map shows only the limb that matters, larger. */
export const FULL_VIEWBOX = "0 0 220 516";
export const REGION_VIEWBOX: Record<string, string> = {
  CERVICAL: "0 62 220 238",
  LUMBOSACRAL: "0 186 220 330",
};

/** Patient side for the half on the left of the screen, per view. */
export const SCREEN_LEFT_SIDE: Record<View, "RIGHT" | "LEFT"> = { front: "RIGHT", back: "LEFT" };

export const zoneKey = (zone: string, side: "RIGHT" | "LEFT") => `${zone}-${side}`;
