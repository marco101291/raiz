import type { NextConfig } from "next";

// /api/v1/* is forwarded to the backend by src/app/api/v1/[...path]/route.ts (RAIZ_API_URL).
const nextConfig: NextConfig = {
  cacheComponents: true,
  partialPrefetching: true,
};

export default nextConfig;
