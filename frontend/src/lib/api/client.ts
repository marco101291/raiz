import type { components } from "./schema";

/** Tipos generados desde el OpenAPI del backend (`npm run gen:api`). No se escriben a mano. */
export type Schemas = components["schemas"];
export type AssessmentRequest = Schemas["AssessmentRequest"];
export type AssessmentResponse =
  | Schemas["UrgentReferral"]
  | Schemas["RootCorrelation"]
  | Schemas["NoRootCorrelation"];

/** Error de la API con el ProblemDetail (RFC 9457) que devuelve el backend. */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly problem: { title?: string; detail?: string; errors?: { field: string; message: string }[] },
  ) {
    super(problem.detail ?? `Error ${status}`);
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api/v1${path}`, {
    ...init,
    headers: { Accept: "application/json", ...init?.headers },
  });
  if (!response.ok) {
    const problem = await response.json().catch(() => ({}));
    throw new ApiError(response.status, problem);
  }
  return response.json() as Promise<T>;
}

export const api = {
  redFlags: () => request<Schemas["RedFlagView"][]>("/catalog/red-flags"),
  painOptions: () => request<Schemas["PainOptionsView"]>("/catalog/pain-options"),
  bodyZones: () => request<Schemas["BodyZoneView"][]>("/catalog/body-zones"),
  strengthTests: () => request<Schemas["StrengthTestView"][]>("/catalog/strength-tests"),
  assess: (body: AssessmentRequest) =>
    request<AssessmentResponse>("/assessments", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    }),
};
