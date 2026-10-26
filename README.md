# Raíz

Plataforma **educativa** de orientación musculoesquelética. Una sola app en la que la persona elige en el mapa dónde le duele y se abre el formulario de esa región. Empieza con **columna** (hernia discal y dolor radicular); rodilla, hombro y cadera llegan después como módulos (ver [Módulos por región](#módulos-por-región)).

Ayuda a entender la lesión, relacionar síntomas con estructuras, conocer el tratamiento conservador y saber cuándo hay que ver a un especialista. **No diagnostica ni prescribe**: toda respuesta termina en una derivación a un profesional. En la interfaz se habla de formularios de *evaluación* u *orientación*, nunca de *diagnóstico*.

Tiene dos audiencias:

- **Paciente:** lenguaje claro, triage de alarma primero, enfoque general del tratamiento y derivación siempre.
- **Profesional** (fisios, neurólogos, traumatólogos): territorios radiculares completos, pruebas de provocación, niveles discales, criterios quirúrgicos y plantillas de protocolo de rehabilitación que el profesional ajusta y prescribe.

Este README es la especificación del proyecto. Está escrito para usarse con Claude Code: ver [Instrucciones para Claude Code](#instrucciones-para-claude-code).

---

## Principios no negociables

1. **Triage antes que todo.** Si hay un dato de alarma, la app no correlaciona ni educa: deriva. Urgencias para cauda equina, déficit progresivo, síntomas bilaterales o mielopatía; valoración médica pronta para fiebre, cáncer, trauma, pérdida de peso o dolor nocturno continuo.
2. **Derivación obligatoria.** Es una invariante del dominio: `Assessment` es una `sealed interface` cuyos tres casos exponen `referral()`, y `Referral` no acepta lista de especialistas vacía. No se resuelve en la capa web.
3. **Compatible, no diagnóstico.** El motor dice "estos síntomas son compatibles con L5", nunca "tienes radiculopatía L5". Los territorios se solapan y varían entre personas.
4. **El paciente no recibe prescripción.** En modo paciente se muestra el *enfoque* de cada fase de rehabilitación. Dosis, ejercicios concretos y duración los define un profesional (ver integración con Alivia).
5. **Contenido con fuente.** Cada dato clínico guarda su referencia. Fuentes base: NICE NG59 (*Low back pain and sciatica in over 16s*), guía de la NASS para hernia discal lumbar con radiculopatía, y literatura de neurodinamia y control motor.
6. **Sin datos personales de salud en Raíz.** El servicio es *stateless* respecto a pacientes. Si algún día guarda evaluaciones, aplica la LFPDPPP (datos de salud = datos sensibles) y hay que revisar si entra como software dispositivo médico ante COFEPRIS. Mientras sea educativo y no diagnóstico, el riesgo regulatorio es bajo.

---

## Funciones del módulo de columna

| Módulo | Paciente | Profesional |
|---|---|---|
| Triage de alarma | Cuestionario corto, bloquea el resto si hay alarma | Igual, con criterios clínicos |
| ¿Es una hernia? | Patrones típicos de dolor discal vs. mecánico | Diagnóstico diferencial (facetario, sacroilíaco, cadera, visceral, zóster) |
| Mapa de dermatomas | Tocar zona → raíces compatibles | Raíz → dermatoma, miotoma, reflejo, pruebas, nivel discal |
| Correlación síntoma → raíz | Zonas + debilidad | Zonas + debilidad + reflejos |
| Rehabilitación | Enfoque por fase y criterios para avanzar | Plantillas de protocolo con dosis editables |
| Cuándo operar | Señales para consultar a un cirujano | Indicaciones absolutas y relativas |
| Tiempos de recuperación | Curva esperada por fase | Historia natural, reabsorción, factores pronósticos |

---

## Arquitectura

```
raiz/
├── backend/            Java 21 · Spring Boot 4 · JPA · Flyway · PostgreSQL
│   └── src/main/java/com/raiz/
│       ├── domain/     Java puro, sin Spring ni JPA (ya implementado, ver abajo)
│       │   ├── model/        NerveRoot, BodyZone, MotorAction, Reflex, RedFlag, SpinalRegion,
│       │   │                 PainQuality, Aggravator, Reliever, PainDistribution
│       │   ├── assessment/   CorrelationEngine, PainPatternClassifier, SymptomReport, PainProfile,
│       │   │                 Assessment (sealed), Evidence (sealed), PatternInsight, Referral...
│       │   └── port/         NerveRootCatalog (puerto de salida)
│       ├── persistence/  Entidades JPA + adaptador JpaNerveRootCatalog   (pendiente)
│       ├── web/          Controladores REST, DTOs, ProblemDetail           (pendiente)
│       └── config/       Wiring del motor como @Bean                       (pendiente)
└── frontend/           Next.js (App Router) · TypeScript · mapa SVG       (pendiente)
```

Arquitectura hexagonal ligera: el dominio no conoce Spring. El motor recibe un `NerveRootCatalog` y la capa de persistencia lo implementa con JPA. Esto permite probar el motor con un catálogo fijo en memoria, sin base de datos.

### Lo que ya existe

- Modelo de dominio para triage, cuestionario de dolor, correlación y derivación.
- `PainPatternClassifier`: lectura del patrón de dolor ("¿es una hernia?").
- `CorrelationEngine`: triage → patrón de dolor → correlación con puntuación → derivación.
- `CorrelationEngineTest` (JUnit 5 + AssertJ) con 15 escenarios. El dominio se compiló y se verificó con `javac` en esos mismos escenarios; la capa Spring todavía no.

### Motor de evaluación

1. **Triage.** Si hay `RedFlag` reportada, o el motor detecta hallazgos en ambos lados de una misma región (las dos piernas o los dos brazos), → `UrgentReferral`. Este último se agrega como `BILATERAL_SYMPTOMS` en `inferred`, separado de lo que marcó la persona (`reported`), para que la interfaz explique por qué. Alarma de urgencia → `EMERGENCY_NOW`; si no → `PROMPT`. No se evalúa nada más.
2. **Patrón de dolor** (`PainPatternClassifier`, si llegó `PainProfile`), por prioridad:
   - `RADICULAR`: algún descriptor neuropático (ardor, hormigueo, adormecimiento, descarga) **y** el dolor baja por debajo de la rodilla o el codo, o se marcaron zonas en el mapa.
   - `DISCOGENIC`: 2 o más rasgos entre empeorar sentado, al flexionar, al toser o al cargar, y mejorar acostado o caminando.
   - `EXTENSION_SENSITIVE`: empeora al extender o estar de pie y mejora sentado (más compatible con origen facetario).
   - `NONSPECIFIC`: nada de lo anterior.
   - Cronicidad: agudo < 6 semanas, subagudo 6–12, crónico > 12.
3. **Sin hallazgos neurológicos** → `NoRootCorrelation` con la lectura del patrón. Si el patrón es radicular pero no se usó el mapa, la nota invita a marcar zonas.
4. **Correlación.** Zonas, debilidad y reflejos llegan con su lado (`Sided<T>`). Para cada raíz **y cada lado** se acumula evidencia (`Evidence` sealed):
   - `SensoryMatch` (zona en su dermatoma): **2 puntos**
   - `MotorMatch` (debilidad en su miotoma): **3 puntos**
   - `ReflexMatch` (reflejo disminuido): **3 puntos**

   Matches con puntuación > 0 (por ejemplo "L5 derecha"), ordenados por puntos, orden anatómico y lado. La evidencia de un lado nunca suma a la del otro.
5. **Derivación**, en este orden:
   - Evidencia motora o refleja → `PROMPT` a cirujano de columna / neurólogo.
   - Dolor ≥ 8/10 → `PROMPT` a médico (control del dolor).
   - Síntomas de más de 12 semanas → `ROUTINE` con cirujano de columna primero.
   - Resto → `ROUTINE` a rehabilitación, fisioterapia o columna.

Los pesos y umbrales son heurísticos y deben revisarse con un profesional antes de publicar.

---

## API (v1)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/v1/nerve-roots?region=LUMBOSACRAL&audience=PATIENT` | Catálogo; la audiencia controla qué campos salen |
| `GET` | `/api/v1/nerve-roots/{code}?audience=PROFESSIONAL` | Detalle de una raíz |
| `POST` | `/api/v1/assessments` | Recibe `SymptomReport`, devuelve `Assessment` |
| `GET` | `/api/v1/rehab/protocols?pattern=RADICULAR_IRRITATION&audience=PATIENT` | Enfoque o plantilla de rehabilitación |
| `GET` | `/api/v1/catalog/body-zones` | Zonas para el mapa SVG (id + etiqueta) |
| `GET` | `/api/v1/catalog/red-flags` | Datos de alarma para el triage |

Además: `GET /api/v1/catalog/pain-options` (descriptores, agravantes, aliviantes y, por región, hasta dónde llega el dolor) y `GET /api/v1/catalog/strength-tests` (pruebas guiadas), para que el frontend no duplique etiquetas.

Ejemplo de `POST /api/v1/assessments`:

```json
{
  "redFlags": [],
  "pain": {
    "intensity": 4,
    "durationDays": 9,
    "reach": [{ "region": "LUMBOSACRAL", "distribution": "PROXIMAL" }],
    "qualities": ["BURNING", "TINGLING"],
    "aggravators": ["SITTING", "FORWARD_BENDING"],
    "relievers": ["LYING_DOWN"]
  },
  "sensoryZones": [
    { "finding": "GROIN", "side": "RIGHT" },
    { "finding": "UPPER_ANTERIOR_THIGH", "side": "RIGHT" }
  ],
  "weakness": [],
  "diminishedReflexes": []
}
```

Respuesta: JSON con discriminador `type` (`URGENT_REFERRAL`, `ROOT_CORRELATION`, `NO_ROOT_CORRELATION`), mapeado con `switch` exhaustivo sobre la sealed interface. Siempre incluye `referral` y `pattern`. Errores con `ProblemDetail` (RFC 9457); las validaciones del dominio (`IllegalArgumentException`) se traducen a 400. Documentación OpenAPI con springdoc.

Con `audience=PATIENT` nunca se exponen `clinicalNotes`, `provocationTests` ni dosis de protocolo.

---

## Formularios

La evaluación del paciente es un asistente de varios pasos. El modo profesional usa los mismos datos en una sola pantalla densa.

### Flujo (modo paciente)

```
1. Triage ──alarma──────────────────────────────▶ 5. Resultado (solo derivación)
   │ sin alarma
   ▼
2. Cuestionario de dolor
   │
   ▼
3. Mapa corporal ── opcional si el dolor no baja desde ninguna región (todo AXIAL_ONLY)
   │
   ▼
4. Fuerza guiada ── solo si hay irradiación o zonas marcadas
   │
   ▼
5. Resultado
```

- Barra de progreso y botón "Atrás" en cada paso. Cambiar una respuesta anterior recalcula qué pasos aplican.
- El progreso se guarda **solo en el navegador** (`sessionStorage`, envuelto en try/catch). Raíz no guarda evaluaciones en el servidor.
- Se envía una sola petición al final, en el paso 5. Excepción: si el triage marca alarma, se envía en ese momento y se salta al resultado.

### Paso 1. Triage

Una pantalla con las preguntas de `RedFlag` en sí/no, formuladas en lenguaje claro ("¿Sientes adormecida la zona entre las piernas o los genitales?"). Todas obligatorias: no se puede avanzar sin responder cada una. Si alguna es "sí", el siguiente botón cambia a "Ver qué hacer".

### Paso 2. Cuestionario de dolor (`PainProfile`)

| Campo | Control | Validación |
|---|---|---|
| `intensity` | Slider 0–10 con descriptores (0 sin dolor, 10 el peor imaginable) | Obligatorio, entero 0–10 |
| Inicio del episodio | Selector de fecha "¿Cuándo empezó esta vez?" → el cliente calcula `durationDays` | Obligatorio, no futura |
| `reach[].region` | "¿Dónde empieza el dolor?": cuello, espalda baja o ambos (chips) | Obligatorio, al menos uno |
| `reach[].distribution` | "¿Hasta dónde llega?", una pregunta por región con tres opciones y silueta: hasta el codo o la mano para el cuello, hasta la rodilla o el pie para la espalda baja | Obligatorio por cada región elegida |
| `qualities` | Chips de selección múltiple | Al menos uno |
| `aggravators` | Chips de selección múltiple | Opcional |
| `relievers` | Chips de selección múltiple; "Nada lo alivia" excluye a los demás | Opcional |

### Paso 3. Mapa corporal (`sensoryZones`)

- Cuerpo en SVG con vistas anterior y posterior. Cada `<path>` lleva `id` = `BodyZone` + lado (p. ej. `GREAT_TOE-RIGHT`), y se envía como `{ "finding": "GREAT_TOE", "side": "RIGHT" }`.
- Ojo con la vista: en la vista anterior la derecha del paciente queda a la izquierda de la pantalla. Etiquetar "Derecha" e "Izquierda" en el SVG.
- Tocar una zona la marca; tocar otra vez la desmarca. Debajo del mapa, lista de zonas marcadas con opción de quitar.
- **Alternativa accesible obligatoria:** la misma selección como lista de casillas agrupadas por región. En móvil las zonas pequeñas (dedos) se eligen mejor así.

### Paso 4. Fuerza guiada (`weakness`)

Pruebas que una persona puede hacer sola, con instrucciones, ilustración y aviso de seguridad (apoyarse en una pared; detenerse si el dolor sube claramente).

| Prueba | Cómo | `MotorAction` | Raíces |
|---|---|---|---|
| Caminar de talones | 10 pasos con la punta del pie levantada | ANKLE_DORSIFLEXION | L4, L5 |
| Caminar de puntas | 10 pasos sobre la punta de los pies | ANKLE_PLANTAR_FLEXION | S1 |
| Elevación de talón a una pierna | 10 repeticiones por lado, apoyado en la pared; comparar | ANKLE_PLANTAR_FLEXION | S1 |
| Dedo gordo contra resistencia | Levantar el dedo gordo mientras un dedo de la mano lo empuja hacia abajo | GREAT_TOE_EXTENSION | L5 |
| Levantarse de una silla con una pierna | Con apoyo de una mano; comparar lados | KNEE_EXTENSION | L3, L4 |
| Apoyo en una pierna | 20 segundos; la cadera del lado contrario no debe caer | HIP_ABDUCTION | L5 |

Respuestas por prueba: "Igual en ambos lados", "Más débil del lado derecho", "Más débil del lado izquierdo", "No pude por dolor", "No la hice". **Solo las opciones de "más débil" cuentan como `weakness`, con su lado.** "No pude por dolor" no es debilidad y se muestra como nota en el resultado.

En cervical la autoexploración es poco fiable. En lugar de pruebas se pregunta por síntomas funcionales ("se me caen las cosas", "me cuesta levantar el brazo"), que se muestran en el resultado como motivo para exploración profesional, sin mapearse a `weakness`.

### Paso 5. Resultado

Orden fijo en pantalla:

1. **Derivación** (siempre arriba, color según `urgency`): a quién ver y con qué prisa.
2. **Patrón de dolor**: tipo, rasgos que lo apoyan y cronicidad.
3. **Raíces compatibles** con su lado ("L5 derecha"): cada una con su evidencia, enlace a su ficha y resaltado en el mapa.
4. **Mientras tanto**: enfoque de la fase 1 de rehabilitación, solo si no hay alarma ni déficit.
5. **Aviso educativo** (`Referral.DISCLAIMER`).

Si hubo alarma, la pantalla muestra únicamente la derivación y los datos de alarma. Si la alarma se infirió del mapa o de las pruebas (síntomas en ambos lados), se dice explícitamente: "Marcaste síntomas en ambas piernas".

Botón "Descargar resumen" (PDF generado en el navegador) para llevarlo a la consulta: respuestas, mapa marcado y resultado.

### Modo profesional

Mismos campos en una sola página, más:
- **Reflejos** por lado: normal, disminuido o abolido. La asimetría entre lados es lo que tiene valor clínico. "Disminuido" y "abolido" se envían en `diminishedReflexes`.
- **Fuerza** por movimiento con escala MRC 0–5. Menos de 5 se envía como `weakness`.
- **Pruebas de provocación** (SLR con ángulo, slump, prueba femoral, Spurling). Se guardan en el resumen; el motor todavía no las usa.

### Escalas

- **Intensidad (NRS 0–10):** libre de uso, ya incluida en el paso 2.
- **Función:** la PSFS (escala funcional específica del paciente) suele considerarse de uso libre; el índice de Oswestry (lumbar) y el NDI (cervical) tienen derechos de autor y algunos usos requieren licencia. **Verificar términos antes de incluir cualquiera.**
- En Raíz las escalas son una foto del momento y no se guardan. El seguimiento en el tiempo le corresponde a Alivia.

### Validación

- Los tipos del frontend se generan del OpenAPI (`openapi-typescript`) para que enums y etiquetas no se desincronicen.
- Esquemas Zod en el frontend (React Hook Form + `zodResolver`) para errores inmediatos por campo.
- Bean Validation en los DTOs del backend, y las invariantes finales en los constructores del dominio. La validación del cliente es comodidad; la del servidor es la que cuenta.

---

## Catálogo clínico (contenido semilla para Flyway)

Patrones clásicos. En la columna lumbar, la hernia **posterolateral** comprime la raíz que *desciende* (L4-L5 → L5); la **foraminal o extraforaminal** comprime la raíz que *sale* (L4-L5 → L4). En la cervical, la raíz es la del número inferior (C5-C6 → C6).

### Cervical

| Raíz | Dermatoma (`BodyZone`) | Miotoma (`MotorAction`) | Reflejo | Nivel típico | Pruebas |
|---|---|---|---|---|---|
| C5 | LATERAL_SHOULDER | SHOULDER_ABDUCTION, ELBOW_FLEXION | BICEPS | C4-C5 | Spurling, ULTT |
| C6 | LATERAL_FOREARM, THUMB, INDEX_FINGER | WRIST_EXTENSION, ELBOW_FLEXION | BRACHIORADIALIS, BICEPS | C5-C6 | Spurling, ULTT1 (mediano) |
| C7 | MIDDLE_FINGER | ELBOW_EXTENSION, WRIST_FLEXION | TRICEPS | C6-C7 | Spurling, ULTT1 |
| C8 | RING_FINGER, LITTLE_FINGER, MEDIAL_FOREARM | FINGER_FLEXION | — | C7-T1 | ULTT3 (cubital) |
| T1 | MEDIAL_ARM | FINGER_ABDUCTION | — | T1-T2 | ULTT3 |

### Lumbosacra

| Raíz | Dermatoma (`BodyZone`) | Miotoma (`MotorAction`) | Reflejo | Nivel típico | Pruebas |
|---|---|---|---|---|---|
| L1 | GROIN | HIP_FLEXION | — | T12-L1 posterolateral; L1-L2 foraminal | Prueba femoral (débil) |
| L2 | GROIN, UPPER_ANTERIOR_THIGH | HIP_FLEXION, HIP_ADDUCTION | — | L1-L2 posterolateral; L2-L3 foraminal | Prueba femoral (prone knee bend) |
| L3 | ANTERIOR_THIGH, MEDIAL_KNEE | KNEE_EXTENSION, HIP_ADDUCTION | PATELLAR | L2-L3 posterolateral; L3-L4 foraminal | Prueba femoral |
| L4 | MEDIAL_KNEE, MEDIAL_LEG, MEDIAL_MALLEOLUS | KNEE_EXTENSION, ANKLE_DORSIFLEXION | PATELLAR | L3-L4 posterolateral; L4-L5 foraminal | Prueba femoral; SLR |
| L5 | LATERAL_LEG, DORSUM_FOOT, GREAT_TOE | GREAT_TOE_EXTENSION, ANKLE_DORSIFLEXION, HIP_ABDUCTION | — (isquiotibial medial, poco fiable) | L4-L5 posterolateral; L5-S1 foraminal | SLR, slump, SLR cruzado |
| S1 | LATERAL_FOOT, SOLE, POSTERIOR_CALF, POSTERIOR_THIGH | ANKLE_PLANTAR_FLEXION, FOOT_EVERSION | ACHILLES | L5-S1 posterolateral | SLR, slump |

Notas para `clinicalNotes`:
- Dolor en ingle o cara anterior del muslo sugiere raíces altas (L1-L3), aunque también puede ser referido desde niveles bajos, facetario o sacroilíaco, o de cadera. Diferencial obligado: patología de cadera, hernia inguinal, pubalgia, causas viscerales y zóster.
- El SLR positivo entre 30° y 70° sugiere tensión radicular de L4 a S1. El SLR cruzado es más específico que sensible.
- El tamaño de la hernia en la resonancia se correlaciona mal con los síntomas. Las extrusiones y los secuestros tienden a reabsorberse más que las protrusiones.

---

## Rehabilitación

### Modelo de dominio

```
RehabProtocol
  pattern: ClinicalPattern        MECHANICAL_LBP | RADICULAR_IRRITATION | RADICULOPATHY_WITH_DEFICIT | POST_SURGICAL
  phases: List<RehabPhase>

RehabPhase
  name, goal
  typicalDuration: Range          orientativo, nunca el criterio para avanzar
  entryCriteria / exitCriteria: List<Criterion>
  approach: String                lo que ve el paciente
  components: List<ExerciseComponent>

ExerciseComponent (sealed)
  Education | Aerobic | MotorControl | Mobility | Neurodynamics | Strength | Plyometric | SportSpecific
  dosage: Dosage                  solo modo profesional; editable al prescribir
  contraindicatedWhen: List<Criterion>
```

**La progresión es por criterios, no por calendario.** La duración es orientativa: se avanza cuando se cumplen los criterios de salida.

### Plantilla: irritación radicular por protrusión o hernia, sin déficit

| Fase | Duración orientativa | Enfoque | Componentes | Para avanzar |
|---|---|---|---|---|
| 1. Calmar | 0–2 semanas | Mantenerse activo sin provocar. Evitar el reposo en cama. | Educación (historia natural favorable), caminata en tramos tolerables, posiciones de alivio, preferencia direccional si existe (McKenzie) | Dolor estable o en descenso; síntomas que se centralizan; caminar 15–20 min sin empeorar |
| 2. Recuperar movimiento | 2–6 semanas | Devolver movilidad y tolerancia a estar sentado | Control motor de tronco, movilidad de cadera y torácica, deslizamientos neurales (no tensores), aeróbico de bajo impacto | Irradiación mínima o ausente; flexión e inclinación sin reproducir síntomas en pierna |
| 3. Fortalecer | 6–12 semanas | Carga progresiva | Fuerza de cadena posterior y tronco, bisagra de cadera, carga axial gradual | Tolera carga moderada sin reaparición de síntomas a las 24 h |
| 4. Volver | 3 meses o más | Regreso al deporte y al trabajo físico | Pliometría, cambios de dirección, gestos del deporte; volumen progresivo | Sin síntomas con la carga del deporte; después, mantenimiento 2–3 veces por semana de forma indefinida |

Regla del día siguiente: si un ejercicio deja más dolor o síntomas neurales a las 24 h, se baja la dosis; no se abandona.

### Sobre "natación 6 meses" y prescripciones fijas

La plataforma no debe recomendar una actividad concreta con duración fija. La evidencia respalda el **ejercicio aeróbico sostenido que la persona tolere y disfrute** (caminar, bicicleta, natación, caminar en agua) como parte del tratamiento, no uno en particular por encima de los demás. La natación es buena opción de bajo impacto, con matices: crol y espalda suelen tolerarse bien con buena técnica; braza y mariposa extienden la zona lumbar y cervical y pueden irritar en fase aguda. En el modelo esto se representa como `Aerobic` con lista de modalidades, contraindicaciones por fase y dosis que define el profesional.

### Otras plantillas a definir

- `MECHANICAL_LBP`: dolor lumbar sin irradiación.
- `RADICULOPATHY_WITH_DEFICIT`: mismo esqueleto con vigilancia neurológica en cada sesión y criterios de escalamiento a cirugía.
- `POST_SURGICAL`: microdiscectomía; los tiempos los marca el cirujano.

---

## Cuándo considerar cirugía

- **Urgente:** síndrome de cauda equina; déficit motor grave o progresivo.
- **Electiva, a valorar:** dolor radicular incapacitante que persiste pese a 6–12 semanas de tratamiento conservador bien hecho, con imagen que concuerde con la clínica.
- **No es indicación por sí sola:** el tamaño de la hernia en la resonancia.
- La cirugía suele acelerar el alivio del dolor de pierna; a largo plazo, los resultados de cirugía y tratamiento conservador tienden a igualarse. Es una decisión compartida con el especialista.

---

## Módulos por región

Una sola app con un **núcleo común** y un **módulo por región del cuerpo**. Se organiza por región, no por especialidad: un fisio ve rodillas, hombros y espaldas, y la diferencia entre paciente y profesional ya la resuelve `audience` dentro de cada módulo.

### Por qué así

- Un formulario único con todo sería largo, haría preguntas irrelevantes y mezclaría lógicas distintas. La columna se razona con dermatomas y miotomas; rodilla y hombro, con el mecanismo de la lesión, los síntomas mecánicos y las pruebas ortopédicas. Cada módulo necesita su propio motor.
- Formularios totalmente separados duplicarían el triage, el cuestionario de dolor, el mapa, la derivación y el PDF, y perderían lo más importante: **las regiones se cruzan**. Un hombro puede doler por C5; una ingle, por L1-L3, la cadera o el pubis.

### Núcleo común (ya existe en parte)

Triage de alarma, cuestionario de dolor (`PainProfile`), mapa corporal con lateralidad, derivación (`Referral`), aviso educativo y PDF de resumen. Cada módulo agrega sus propios datos de alarma (p. ej. rodilla: imposibilidad de apoyar tras un trauma; hombro: deformidad tras una caída).

### Flujo

```
Inicio: "¿Dónde te duele?" (mapa)
   │
   ▼
Router ──▶ módulo de la región elegida
   │        └─ si la zona es de cruce, sugiere también el módulo de columna
   ▼
Triage común + triage del módulo
   ▼
Cuestionario de dolor común
   ▼
Formulario del módulo (preguntas y pruebas propias)
   ▼
Resultado (derivación siempre arriba; si hubo cruce, ambos resultados)
```

### Regiones y cruces con columna

| `BodyRegion` | Módulo | Revisar también columna cuando… | Raíces de cruce |
|---|---|---|---|
| `SPINE` | Columna (actual) | — | — |
| `SHOULDER` | Hombro | Hay hormigueo o ardor, o el dolor baja del cuello | C5, C6 |
| `ELBOW` | Codo | Síntomas neuropáticos o dolor cervical asociado | C6, C7 |
| `WRIST_HAND` | Muñeca y mano | Hormigueo en dedos (diferencial con túnel carpiano y canal de Guyon) | C6, C7, C8 |
| `HIP_GROIN` | Cadera e ingle | Dolor lumbar asociado o ardor en ingle o muslo | L1, L2, L3 |
| `KNEE` | Rodilla | Dolor sin trauma, con ardor o hormigueo, o que baja de la espalda | L3, L4 |
| `ANKLE_FOOT` | Tobillo y pie | Hormigueo o adormecimiento en el pie | L4, L5, S1 |

Regla del router: si el `PainProfile` tiene descriptores neuropáticos, o la distribución viene de la espalda o el cuello, el resultado del módulo periférico incluye una sección "También podría venir de la columna" con la correlación radicular de las raíces de cruce.

### Contrato de un módulo

```java
public interface RegionModule {
    BodyRegion region();
    Set<RedFlag> regionRedFlags();                     // se suman al triage común
    FormDefinition form(Audience audience);            // preguntas y pruebas propias, como datos
    ModuleAssessment assess(CoreIntake core, ModuleAnswers answers);
    Set<BodyRegion> crossReferences(CoreIntake core);  // p. ej. SHOULDER → SPINE si hay ardor
}
```

- Spring inyecta todos los módulos (`List<RegionModule>`) en un `RegionRouter` y los indexa por región. Agregar una región = agregar una clase, sin tocar el núcleo (principio abierto/cerrado).
- `ModuleAssessment` también es una `sealed interface` que siempre lleva `Referral`.

### Formularios como datos

El backend describe cada formulario (`FormDefinition`: secciones, preguntas, tipo de control, opciones, condiciones de visibilidad, validaciones) y el frontend tiene **un solo renderizador genérico**. Los controles especiales (mapa corporal, pruebas guiadas con ilustración) son componentes propios que el renderizador reconoce por tipo. Así, un módulo nuevo no exige escribir pantallas nuevas.

### Contenido de los siguientes módulos (a validar clínicamente)

- **Rodilla:** mecanismo (giro con pie fijo, golpe, sin trauma), chasquido, inflamación inmediata vs. en horas, bloqueo, fallo o inestabilidad, dolor al subir escaleras o en cuclillas. Profesional: Lachman, cajón anterior, McMurray, Thessaly, estrés en varo y valgo. Orienta hacia ligamentos, meniscos, dolor patelofemoral o tendinopatía.
- **Hombro:** dolor nocturno al acostarse sobre ese lado, arco doloroso, pérdida de movilidad activa vs. pasiva, debilidad al elevar el brazo, antecedente de luxación. Profesional: Neer, Hawkins-Kennedy, Jobe, drop arm, aprensión. Orienta hacia manguito rotador, capsulitis, inestabilidad o compromiso cervical.
- **Cadera e ingle:** dolor en ingle al flexionar y rotar, chasquido, dolor al estar sentado mucho tiempo, dolor en pubis con el esfuerzo. Profesional: FADIR, FABER, aducción resistida. Diferencial obligado con columna lumbar, pubalgia y hernia inguinal.

### Organización del código

```
com.raiz.core            triage, PainProfile, Sided, Referral, router, FormDefinition
com.raiz.modules.spine   lo que hoy está en domain (NerveRoot, CorrelationEngine...)
com.raiz.modules.knee    ...
```

No hay que mover el código actual todavía. La reorganización se hace cuando entre el segundo módulo, no antes.

---

## Integración con Alivia

Alivia (Next.js + FastAPI + Prisma/PostgreSQL) es la plataforma multi-tenant de seguimiento de resultados clínicos para fisioterapeutas. Raíz y Alivia se complementan:

| | Raíz | Alivia |
|---|---|---|
| Rol | Conocimiento: catálogo clínico, motor de correlación, plantillas de protocolo | Práctica: pacientes, prescripción, seguimiento |
| Datos de pacientes | Ninguno | Sí (multi-tenant, datos sensibles) |
| Quién decide | Nadie: educa y deriva | El profesional prescribe y ajusta |

Flujo:

1. El paciente usa Raíz: triage, mapa y correlación, y recibe una derivación.
2. La derivación puede incluir un enlace a profesionales que usan Alivia (directorio opcional, a futuro).
3. En Alivia, el fisio consume la API de Raíz en modo profesional: consulta la correlación durante la exploración e **instancia una plantilla `RehabProtocol`** para el paciente, editando dosis y criterios. La prescripción vive en Alivia.
4. Alivia registra resultados (EVA/NRS, Oswestry o NDI, criterios de fase cumplidos) y avanza las fases por criterio.

Contrato compartido:
- Raíz publica OpenAPI; Alivia genera su cliente a partir de él (`openapi-generator` o `openapi-typescript`).
- IDs estables entre sistemas: códigos de raíz (`L5`), `BodyZone`, `RedFlag`, `ClinicalPattern`, `protocolId` + `version`.
- Las plantillas se versionan: un protocolo prescrito en Alivia queda ligado a la versión de la plantilla con la que se creó.
- A futuro, Alivia puede devolver resultados anonimizados y agregados para calibrar los pesos del motor y la duración orientativa de cada fase.

---

## Hoja de ruta

1. **Backend base:** persistencia JPA + Flyway con el catálogo de esta página, endpoints de raíces y evaluaciones, pruebas de integración.
2. **Frontend:** el asistente de evaluación de la sección [Formularios](#formularios): triage, cuestionario de dolor, mapa SVG (IDs = `BodyZone`), fuerza guiada y pantalla de resultado.
3. **Rehabilitación:** modelo `RehabProtocol`, primera plantilla (irritación radicular sin déficit) y endpoints.
4. **Modo profesional:** autenticación simple, vistas clínicas.
5. **Revisión clínica:** validar contenido y pesos de columna con un fisio y un médico antes de publicar.
6. **Plataforma multirregión:** `FormDefinition` y renderizador genérico, `RegionRouter`, reorganizar en `core` + `modules.spine`.
7. **Módulo de rodilla**, luego hombro y cadera, cada uno con su revisión clínica.
8. **Integración con Alivia:** cliente generado y prescripción a partir de plantillas.

---

## Instrucciones para Claude Code

**Stack y convenciones**
- Java 21, Spring Boot 4 (última 4.x estable), Maven. PostgreSQL 16 con `docker-compose.yml`.
- `domain/` se queda en Java puro: sin anotaciones de Spring ni de JPA. Las entidades JPA viven en `persistence/` y se mapean a los records del dominio.
- Usa Java moderno donde aporte: records, sealed interfaces, `switch` con pattern matching, `List.copyOf`/`Set.copyOf` en constructores compactos.
- Migraciones con Flyway (`V1__schema.sql`, `V2__seed_nerve_roots.sql`, ...). El contenido clínico entra por migraciones, no hardcodeado.
- Enums persistidos como texto (`@Enumerated(EnumType.STRING)`); colecciones de enums con `@ElementCollection`.
- DTOs como records con Bean Validation. Errores con `ProblemDetail`. OpenAPI con springdoc.
- Pruebas: JUnit 5 + AssertJ para dominio; `@SpringBootTest` + MockMvc + Testcontainers (PostgreSQL) para integración.
- Textos visibles al usuario en español.

**Primera tarea sugerida**
> Lee este README y el código de `backend/src/main/java/com/raiz/domain`. Crea `pom.xml`, `docker-compose.yml`, `application.yml`, las migraciones Flyway con el catálogo completo de este README (C5–T1 y L1–S1), las entidades JPA, el adaptador `JpaNerveRootCatalog`, el `@Bean` del `CorrelationEngine` y los endpoints de raíces, catálogos y evaluaciones con soporte de `audience`. Corre `mvn verify` hasta que pasen todas las pruebas.

**Segunda tarea sugerida**
> Crea `frontend/` con Next.js (App Router) y TypeScript. Genera los tipos desde el OpenAPI del backend y construye el asistente de evaluación tal como lo describe la sección Formularios: pasos, lógica condicional, validación con Zod, progreso en `sessionStorage` y pantalla de resultado con la derivación siempre arriba. Empieza por el triage y el cuestionario de dolor; el mapa SVG va después.

**Reglas que no se rompen**
- Ninguna respuesta de evaluación sin `referral`.
- Con `audience=PATIENT` no salen notas clínicas, pruebas de provocación ni dosis.
- Todo contenido clínico lleva fuente y queda pendiente de revisión clínica (hoja de ruta, paso 5). Si no hay fuente para un dato, deja un `TODO(clinico)` y avisa.
