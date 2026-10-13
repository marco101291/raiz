import type { NextConfig } from "next";

// /api/v1/* se reenvía al backend desde src/app/api/v1/[...path]/route.ts (RAIZ_API_URL).
const nextConfig: NextConfig = {
  cacheComponents: true,
  partialPrefetching: true,
};

export default nextConfig;
