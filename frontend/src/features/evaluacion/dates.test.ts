import { describe, expect, it } from "vitest";
import { daysBetween, describeDuration, isIsoDate, todayIso } from "./dates";

describe("dates", () => {
  it("todayIso usa la fecha local", () => {
    expect(todayIso(new Date(2026, 9, 3, 23, 30))).toBe("2026-10-03");
  });

  it("isIsoDate rechaza fechas imposibles", () => {
    expect(isIsoDate("2026-10-13")).toBe(true);
    expect(isIsoDate("2026-02-30")).toBe(false);
    expect(isIsoDate("13/10/2026")).toBe(false);
    expect(isIsoDate("")).toBe(false);
  });

  it("daysBetween cuenta días completos aunque cruce un cambio de horario", () => {
    expect(daysBetween("2026-10-04", "2026-10-13")).toBe(9);
    expect(daysBetween("2026-03-01", "2026-04-15")).toBe(45);
    expect(daysBetween("2026-10-13", "2026-10-13")).toBe(0);
  });

  it("describeDuration habla en días y luego en semanas", () => {
    expect(describeDuration(0)).toBe("Empezó hoy.");
    expect(describeDuration(9)).toBe("Empezó hace 9 días.");
    expect(describeDuration(30)).toBe("Empezó hace 4 semanas.");
    expect(describeDuration(120)).toBe("Empezó hace más de 12 semanas (17 semanas).");
  });
});
