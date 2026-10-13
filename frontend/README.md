# Raíz: frontend

Asistente de evaluación de Raíz (ver el README de la raíz del repositorio). Next.js (App Router) y TypeScript.

```bash
npm install
npm run dev          # http://localhost:3000; /api/v1/* se reenvía al backend
npm run gen:api      # regenera src/lib/api/schema.d.ts desde el OpenAPI del backend
npm run typecheck && npm run lint && npm run build
```

El backend se toma de `RAIZ_API_URL` (por defecto `http://localhost:8080`).
