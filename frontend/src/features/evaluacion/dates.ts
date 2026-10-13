/**
 * Fechas como "AAAA-MM-DD" en la zona horaria de la persona. Se guarda la fecha de inicio y no los
 * días transcurridos: si recarga mañana, la duración sigue siendo correcta.
 */
export function todayIso(now = new Date()): string {
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
}

export function isIsoDate(value: string): boolean {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
  const [y, m, d] = value.split("-").map(Number);
  const date = new Date(Date.UTC(y, m - 1, d));
  return date.getUTCFullYear() === y && date.getUTCMonth() === m - 1 && date.getUTCDate() === d;
}

/** Días completos entre dos fechas ISO. En UTC para que el cambio de horario no reste un día. */
export function daysBetween(fromIso: string, toIso: string): number {
  const utc = (iso: string) => {
    const [y, m, d] = iso.split("-").map(Number);
    return Date.UTC(y, m - 1, d);
  };
  return Math.round((utc(toIso) - utc(fromIso)) / 86_400_000);
}

export function describeDuration(days: number): string {
  if (days === 0) return "Empezó hoy.";
  if (days === 1) return "Empezó ayer.";
  if (days < 14) return `Empezó hace ${days} días.`;
  const weeks = Math.floor(days / 7);
  if (days < 84) return `Empezó hace ${weeks} semanas.`;
  return `Empezó hace más de 12 semanas (${weeks} semanas).`;
}
