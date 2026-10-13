// @vitest-environment node
import { afterEach, describe, expect, it, vi } from "vitest";
import { GET, POST } from "./route";

const context = (...path: string[]) => ({ params: Promise.resolve({ path }) });

describe("proxy /api/v1", () => {
  afterEach(() => vi.restoreAllMocks());

  it("reenvía la ruta, la query y el cuerpo al backend", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response('{"type":"NO_ROOT_CORRELATION"}', { status: 200, headers: { "Content-Type": "application/json" } }),
    );

    const response = await POST(
      new Request("http://localhost:3000/api/v1/assessments?x=1", {
        method: "POST",
        headers: { "Content-Type": "application/json", Cookie: "no-debe-pasar" },
        body: "{}",
      }),
      context("assessments"),
    );

    const [url, init] = fetchMock.mock.calls[0];
    expect(String(url)).toBe("http://localhost:8080/api/v1/assessments?x=1");
    expect(init?.method).toBe("POST");
    expect(init?.body).toBe("{}");
    expect(new Headers(init?.headers).get("cookie")).toBeNull();
    expect(response.status).toBe(200);
    expect(await response.json()).toEqual({ type: "NO_ROOT_CORRELATION" });
  });

  it("conserva el estado y el ProblemDetail de los errores del backend", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response('{"status":404}', { status: 404, headers: { "Content-Type": "application/problem+json" } }),
    );

    const response = await GET(new Request("http://localhost:3000/api/v1/nerve-roots/X9"), context("nerve-roots", "X9"));

    expect(response.status).toBe(404);
    expect(response.headers.get("content-type")).toBe("application/problem+json");
  });

  it("con el backend caído responde 503 con ProblemDetail en vez de lanzar", async () => {
    vi.spyOn(globalThis, "fetch").mockRejectedValue(new TypeError("fetch failed"));
    vi.spyOn(console, "warn").mockImplementation(() => {});

    const response = await GET(new Request("http://localhost:3000/api/v1/catalog/red-flags"), context("catalog", "red-flags"));

    expect(response.status).toBe(503);
    expect(response.headers.get("content-type")).toBe("application/problem+json");
    expect(await response.json()).toMatchObject({ status: 503, title: "Servicio no disponible" });
  });
});
