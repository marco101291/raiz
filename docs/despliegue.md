# Despliegue

Raíz se despliega en tres servicios con plan gratuito:

| Parte | Servicio | Configuración en el repo |
| --- | --- | --- |
| Frontend (Next.js) | Netlify | `netlify.toml` |
| Backend (Spring Boot) | Render, desde un Dockerfile | `render.yaml`, `backend/Dockerfile` |
| Base de datos (PostgreSQL) | Neon | Variables `RAIZ_DB_*` en Render |

```
navegador ──▶ Netlify (Next.js) ──/api/v1/*──▶ Render (Spring Boot) ──▶ Neon (PostgreSQL)
                                                     ▲
                        GitHub Actions: ping cada 5 min (keep-awake.yml)
```

El navegador solo habla con Netlify. La ruta `/api/v1/*` del frontend reenvía las peticiones al backend, así que no hace falta CORS y la URL del backend nunca llega al cliente.

## Límites del plan gratuito

- **Render** duerme el servicio tras 15 minutos sin tráfico; despertarlo tarda alrededor de un minuto, más de lo que espera una función de Netlify. Por eso el workflow `keep-awake.yml` le hace ping cada 5 minutos: un mes tiene como máximo 744 horas y el plan incluye 750. GitHub puede retrasar los workflows programados, así que alguna vez puede dormirse; la persona verá "No pudimos cargar las preguntas" y al reintentar ya funcionará. GitHub también desactiva los workflows programados tras 60 días sin actividad en el repositorio.
- **Neon** suspende la base tras 5 minutos sin uso y la despierta en menos de un segundo con la siguiente consulta. El ping usa `/actuator/health`, que no consulta la base, y el perfil `prod` cierra las conexiones inactivas, para que la base pueda dormirse y no gaste las horas de cómputo gratuitas.
- **Arranque del backend:** con 512 MB y una fracción de CPU tarda un par de minutos en arrancar después de cada despliegue.

## Pasos

Hazlos en este orden: cada servicio necesita la dirección del anterior.

### 1. Base de datos en Neon

1. Crea una cuenta en [neon.com](https://neon.com) (no pide tarjeta) y un proyecto llamado `raiz`, en la región más cercana a la de Render (por ejemplo, `AWS US East (Ohio)` si Render está en Ohio).
2. En **Connect**, desactiva **Connection pooling** (Flyway necesita la conexión directa) y copia los datos de conexión. Neon muestra algo como:

   ```
   postgresql://neondb_owner:CONTRASEÑA@ep-ejemplo-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
   ```

3. Con eso arma las tres variables para Render. La URL de JDBC es la misma dirección sin el usuario ni la contraseña:

   | Variable | Valor |
   | --- | --- |
   | `RAIZ_DB_URL` | `jdbc:postgresql://ep-ejemplo-123456.us-east-2.aws.neon.tech/neondb?sslmode=require` |
   | `RAIZ_DB_USER` | `neondb_owner` |
   | `RAIZ_DB_PASSWORD` | la contraseña |

No hace falta crear tablas: Flyway aplica las migraciones al arrancar el backend.

### 2. Backend en Render

1. Crea una cuenta en [render.com](https://render.com) entrando con GitHub.
2. **New > Blueprint**, elige el repositorio `raiz`. Render lee `render.yaml` y propone el servicio `raiz-backend` con plan gratuito.
3. Te pedirá las tres variables `RAIZ_DB_*`: pega las del paso anterior.
4. Espera a que el despliegue termine y el servicio aparezca como **Live**. Su dirección será algo como `https://raiz-backend.onrender.com`.
5. Comprueba que responde: `https://raiz-backend.onrender.com/actuator/health` debe devolver `{"status":"UP",...}`.

Render vuelve a desplegar el backend con cada push a `main` que cambie algo en `backend/`.

### 3. Ping para mantenerlo despierto

En GitHub: **Settings > Secrets and variables > Actions > Variables > New repository variable**:

- Nombre: `BACKEND_URL`
- Valor: la dirección de Render, sin `/` al final (`https://raiz-backend.onrender.com`)

Desde ese momento corre el workflow **Keep backend awake**. Puedes lanzarlo a mano en la pestaña **Actions** para probarlo.

### 4. Frontend en Netlify

1. Crea una cuenta en [netlify.com](https://netlify.com) entrando con GitHub.
2. **Add new site > Import an existing project**, elige el repositorio `raiz`. Netlify lee `netlify.toml` (carpeta `frontend`, comando de build y versión de Node), así que no hay que cambiar nada en el formulario.
3. Antes de desplegar, en **Environment variables**, agrega:
   - `RAIZ_API_URL` = la dirección de Render (`https://raiz-backend.onrender.com`)
4. Despliega. Netlify te dará una dirección como `https://raiz-ejemplo.netlify.app`; puedes cambiar el nombre en **Site configuration > Change site name**.
5. Abre la dirección y haz una evaluación completa.

Netlify vuelve a desplegar el frontend con cada push a `main`.

## Si algo falla

- **"No pudimos cargar las preguntas":** el backend está dormido o caído. Abre `/actuator/health` en la dirección de Render; si tarda, está despertando. Revisa que el workflow **Keep backend awake** esté corriendo.
- **El backend no arranca en Render:** revisa los logs del servicio. `Connection refused` o `password authentication failed` son las variables `RAIZ_DB_*`; un error de Flyway suele ser que la URL apunta al pooler de Neon (`-pooler` en el host) en lugar de a la conexión directa.
- **El banner de demo:** está en `frontend/src/components/DemoBanner.tsx`. Se quita cuando termine la revisión clínica.
