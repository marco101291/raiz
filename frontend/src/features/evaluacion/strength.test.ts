import { describe, expect, it } from "vitest";
import { consultationNotes, weaknessFrom } from "./strength";

const plantarFlexion = { id: "ANKLE_PLANTAR_FLEXION", label: "Pararse de puntas" };
const tests = [
  { id: "TOE_WALK", name: "Caminar de puntas", motorAction: plantarFlexion },
  { id: "SINGLE_LEG_HEEL_RAISE", name: "Elevación de talón a una pierna", motorAction: plantarFlexion },
  { id: "HEEL_WALK", name: "Caminar de talones", motorAction: { id: "ANKLE_DORSIFLEXION", label: "Levantar la punta" } },
];

const questions = [
  {
    id: "ARM_RAISE",
    question: "¿Te cuesta levantar el brazo?",
    motorAction: { id: "SHOULDER_ABDUCTION", label: "Separar el brazo del cuerpo" },
  },
  {
    id: "HAND_GRIP",
    question: "¿Te falta fuerza para apretar?",
    motorAction: { id: "FINGER_FLEXION", label: "Cerrar el puño con fuerza" },
  },
];

describe("weaknessFrom", () => {
  it("solo «más débil» cuenta como debilidad, con su lado", () => {
    const answers = { TOE_WALK: "WEAKER_LEFT", HEEL_WALK: "PAIN", SINGLE_LEG_HEEL_RAISE: "SAME" };
    expect(weaknessFrom(tests, answers)).toEqual(["ANKLE_PLANTAR_FLEXION-LEFT"]);
  });

  it("dos pruebas del mismo movimiento se envían una sola vez", () => {
    expect(weaknessFrom(tests, { TOE_WALK: "WEAKER_RIGHT", SINGLE_LEG_HEEL_RAISE: "WEAKER_RIGHT" })).toEqual([
      "ANKLE_PLANTAR_FLEXION-RIGHT",
    ]);
  });
});

describe("consultationNotes", () => {
  it("anota las pruebas que no se pudieron hacer por dolor y los brazos con falta de fuerza", () => {
    const testAnswers = { HEEL_WALK: "PAIN", TOE_WALK: "SKIPPED" };
    const functionalAnswers = { ARM_RAISE: "RIGHT", HAND_GRIP: "NO" };
    expect(consultationNotes(tests, testAnswers, questions, functionalAnswers)).toEqual([
      "No pudiste hacer «Caminar de talones» por dolor.",
      "Te cuesta separar el brazo del cuerpo (brazo derecho).",
    ]);
  });
});
