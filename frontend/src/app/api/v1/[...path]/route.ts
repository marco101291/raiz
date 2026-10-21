/**
 * Proxy to the Spring backend: the browser only talks to Next, so no CORS is needed and the backend URL
 * never reaches the client. It is a Route Handler rather than a rewrite because a rewrite cannot catch the
 * connection error: if the backend is down, this responds with a 503 ProblemDetail.
 */
const API_URL = process.env.RAIZ_API_URL ?? "http://localhost:8080";

type Context = { params: Promise<{ path: string[] }> };

async function proxy(request: Request, { params }: Context): Promise<Response> {
  const { path } = await params;
  const target = new URL(`/api/v1/${path.map(encodeURIComponent).join("/")}`, API_URL);
  target.search = new URL(request.url).search;

  const headers = new Headers();
  for (const name of ["accept", "content-type", "accept-language"]) {
    const value = request.headers.get(name);
    if (value) headers.set(name, value);
  }

  let response: Response;
  try {
    response = await fetch(target, {
      method: request.method,
      headers,
      body: request.method === "GET" ? undefined : await request.text(),
      cache: "no-store",
    });
  } catch {
    // One log line instead of the full ECONNREFUSED stack trace.
    console.warn(`[raiz] Backend no disponible en ${API_URL}`);
    return Response.json(
      {
        title: "Servicio no disponible",
        status: 503,
        detail: "No pudimos conectar con el servicio de evaluación. Inténtalo de nuevo en unos minutos.",
      },
      { status: 503, headers: { "Content-Type": "application/problem+json" } },
    );
  }

  const contentType = response.headers.get("content-type");
  return new Response(response.body, {
    status: response.status,
    headers: contentType ? { "Content-Type": contentType } : undefined,
  });
}

export { proxy as GET, proxy as POST };
