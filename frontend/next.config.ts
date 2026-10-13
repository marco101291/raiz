import type { NextConfig } from "next";

// El navegador solo habla con Next: /api/v1/* se reenvía al backend de Spring.
// Así no hace falta configurar CORS y la URL del backend no llega al cliente.
const apiUrl = process.env.RAIZ_API_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  cacheComponents: true,
  partialPrefetching: true,
  async rewrites() {
    return [{ source: "/api/v1/:path*", destination: `${apiUrl}/api/v1/:path*` }];
  },
};

export default nextConfig;
