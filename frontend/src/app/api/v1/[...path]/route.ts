/**
 * Proxy hacia el backend de Spring: el navegador solo habla con Next, así que no hace falta CORS y
 * la URL del backend no llega al cliente. Es un Route Handler y no un rewrite porque un rewrite no
 * puede atrapar el error de conexión: si el backend está caído, aquí se responde un ProblemDetail 503.
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
    // Una línea en el log en lugar de la traza completa de ECONNREFUSED.
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
