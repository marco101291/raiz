import type { Schemas } from "@/lib/api/client";

type StrengthTest = Schemas["StrengthTestView"];
type FunctionalQuestion = Schemas["FunctionalQuestionView"];

/** Answers per strength test (README, Forms, step 4). Only "weaker" counts as weakness, with its side. */
export const TEST_ANSWERS = [
  { id: "SAME", label: "Igual en ambos lados" },
  { id: "WEAKER_RIGHT", label: "Más débil del lado derecho" },
  { id: "WEAKER_LEFT", label: "Más débil del lado izquierdo" },
  { id: "PAIN", label: "No pude por dolor" },
  { id: "SKIPPED", label: "No la hice" },
] as const;

/** Answers per functional question for the neck. They never count as weakness. */
export const FUNCTIONAL_ANSWERS = [
  { id: "NO", label: "No" },
  { id: "RIGHT", label: "Sí, el brazo derecho" },
  { id: "LEFT", label: "Sí, el brazo izquierdo" },
  { id: "BOTH", label: "Sí, los dos" },
] as const;

const WEAKER_SIDE: Record<string, "RIGHT" | "LEFT"> = { WEAKER_RIGHT: "RIGHT", WEAKER_LEFT: "LEFT" };

/**
 * Weakness to send to the engine, as "ANKLE_PLANTAR_FLEXION-RIGHT". Two tests can check the same movement
 * (walking on tiptoe and single-leg heel raise): it is sent once.
 */
export function weaknessFrom(tests: StrengthTest[], answers: Record<string, string>): string[] {
  const keys = tests.flatMap((test) => {
    const side = WEAKER_SIDE[answers[test.id]];
    return side ? [`${test.motorAction.id}-${side}`] : [];
  });
  return [...new Set(keys)];
}

const ARM: Record<string, string> = { RIGHT: "brazo derecho", LEFT: "brazo izquierdo", BOTH: "los dos brazos" };

/**
 * What the result shows as worth mentioning to a professional: tests the person could not do because of
 * pain ("No pude por dolor" is not weakness) and everyday tasks where an arm feels weak (README, step 4).
 */
export function consultationNotes(
  tests: StrengthTest[],
  testAnswers: Record<string, string>,
  questions: FunctionalQuestion[],
  functionalAnswers: Record<string, string>,
): string[] {
  const painful = tests
    .filter((test) => testAnswers[test.id] === "PAIN")
    .map((test) => `No pudiste hacer «${test.name}» por dolor.`);
  const weakArms = questions.flatMap((question) => {
    const arm = ARM[functionalAnswers[question.id]];
    const movement = question.motorAction.label.toLowerCase();
    return arm ? [`Te cuesta ${movement} (${arm}).`] : [];
  });
  return [...painful, ...weakArms];
}
