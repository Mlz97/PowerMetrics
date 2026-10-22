# Arquitectura de PowerMetrics

PowerMetrics analiza vídeos de ejercicios con visión artificial: **sentadilla** y **salto vertical con contramovimiento (CMJ)**. La app Android sube el vídeo, el backend Java orquesta el proceso y un servicio Python con MediaPipe extrae las métricas. Este documento es la referencia para retomar el desarrollo: qué hace cada pieza, cómo se comunican y cómo se ejecutan.

> Pendientes y deuda técnica: [PENDIENTES.md](PENDIENTES.md).

## Vista rápida

```mermaid
sequenceDiagram
    participant App as App Android (Kotlin)
    participant BE as Backend (Spring Boot)
    participant PY as Servicio de análisis (FastAPI)

    App->>BE: POST /session {exerciseType}
    BE-->>App: 201 sesión WAITING
    App->>BE: POST /session/{id}/video (multipart + parámetros)
    BE->>BE: guarda vídeo en uploads/, sesión PROCESSING
    BE-->>App: 202 sesión PROCESSING
    BE->>PY: POST /analyze (en segundo plano, @Async)
    PY-->>BE: métricas + ruta del vídeo anotado
    BE->>BE: sesión FINISHED (o ERROR)
    loop cada 2 s, máx. 10 min
        App->>BE: GET /session/{id}
    end
    App->>BE: GET /video/download/{id}
    BE-->>App: vídeo anotado (mp4)
```

## Componentes

| Componente | Carpeta | Stack | Puerto |
|---|---|---|---|
| App móvil | `FEPerfectSquat/` | Kotlin 2.2, Jetpack Compose, Retrofit 2.9, minSdk 24 | — |
| Backend | `BEPerfectSquat/` | Java 17, Spring Boot 3.4, JPA + H2 en memoria | 8080 |
| Servicio de análisis | `PYAnalysisService/` | Python, FastAPI, MediaPipe 0.10.14, OpenCV | 8000 |

## Backend (`BEPerfectSquat`)

Organizado en capas, de fuera hacia dentro:

| Capa | Paquete | Responsabilidad |
|---|---|---|
| API | `api/controller`, `api/dto`, `api/exception` | Adaptador HTTP, contrato público y mapeo de errores |
| Dominio | `domain/entity`, `domain/service`, `domain/repository`, `domain/enums` | Sesiones, vídeos, reglas de estado |
| Infraestructura | `infrastructure/client`, `infrastructure/service`, `infrastructure/config` | Cliente HTTP a Python, almacenamiento en disco, `RestTemplate` |

> Las entidades JPA son a la vez modelo de dominio y de persistencia. No es arquitectura hexagonal estricta: el dominio depende de `AnalysisClient` y de `StorageService` sin puertos intermedios.

### Endpoints

| Método | Ruta | Respuesta | Uso |
|---|---|---|---|
| POST | `/session` | 201 `AnalysisSessionResponse` | Crea una sesión en `WAITING`. Body opcional `{"exerciseType": "SQUAT" o "VERTICAL_JUMP"}`; sin body, `SQUAT` |
| GET | `/session` | 200 `AnalysisSessionResponse[]` | Lista todas las sesiones, la más reciente primero (Histórico y Estadísticas) |
| GET | `/session/{id}` | 200 `AnalysisSessionResponse` | Consulta estado y métricas, con `reps[]` (sentadilla) o `jumps[]` (salto); la app hace polling aquí |
| POST | `/session/{id}/video` | 202 `AnalysisSessionResponse` / 400 / 409 / 500 | Única subida: guarda el vídeo y lanza el análisis. Parámetros opcionales: `weightKg` (barra, sentadilla, admite decimales), `bodyWeightKg` y `heightCm` (salto) |
| GET | `/video?sessionId=` | 200 `VideoResponse` | Datos del vídeo de una sesión |
| GET | `/video/download/{sessionId}` | 200 mp4 / 404 | Descarga el vídeo anotado |
| — | `/h2-console` | — | Consola de la base de datos (solo desarrollo) |

### Ciclo de vida de una sesión

```mermaid
stateDiagram-v2
    [*] --> WAITING: POST /session
    WAITING --> PROCESSING: vídeo subido
    PROCESSING --> FINISHED: Python devuelve resultados
    PROCESSING --> ERROR: fallo o timeout de Python
```

- Solo se acepta vídeo en `WAITING`; en otro estado responde 409 `INVALID_STATE` con el estado actual.
- Vídeo vacío: 400 `BAD_REQUEST` con el motivo. Fallo al escribir o leer en disco: 500 `STORAGE_ERROR` (`StorageException`).
- Rangos válidos (`AnalysisParameters`): `weightKg` 0–500, `bodyWeightKg` 20–300, `heightCm` 100–250. Fuera de rango responde 400 `INVALID_PARAMETERS` con el motivo.
- Solo se guardan resultados en `PROCESSING`; en otro estado responde 409.
- `AnalysisRunner.analyze` corre en otro hilo (`@EnableAsync`). Cualquier excepción deja la sesión en `ERROR`.
- Timeouts hacia Python: 5 s de conexión y 10 min de lectura (`RestTemplateConfig`).

### Modelo de datos

| Entidad | Campos clave | Relación |
|---|---|---|
| `AnalysisSession` | Común: `exerciseType`, `state`, `createdAt`, `videoFps`, `lowFpsWarning`, `fatigueIndex`. Sentadilla: `weightKg`, `totalReps`, `validReps`, `avgConcentricVelocity`, `estimatedRM`. Salto: `bodyWeightKg`, `heightCm`, `totalJumps`, `bestJumpHeightCm`, `avgJumpHeightCm`, `peakPowerW` | 1–1 `Video`, 1–N `RepDetail`, 1–N `JumpDetail` |
| `Video` | `filePath`, `processedFilePath`, `uploadedAt` | pertenece a una sesión |
| `RepDetail` | `repNumber`, `valid`, `minHipAngle`, `minKneeAngle`, `concentricVelocity` | pertenece a una sesión |
| `JumpDetail` | `jumpNumber`, `flightTime`, `heightFlightCm`, `takeoffVelocity`, `minKneeAngle`, `countermovementDepthCm`, `heightDisplacementCm`, `peakPowerW` | pertenece a una sesión; se devuelve en `jumps[]` |

Los vídeos se guardan en `BEPerfectSquat/uploads/` con nombre UUID; el anotado lleva el sufijo `_analyzed.mp4`. La carpeta está en `.gitignore`.

## Servicio de análisis (`PYAnalysisService`)

**Contrato** (`app/models.py`):

| Petición `POST /analyze` | Respuesta |
|---|---|
| `sessionId`, `videoFilePath` (ruta absoluta en disco), `exerciseType` (`SQUAT` por defecto), `weightKg?`, `bodyWeightKg?`, `heightCm?` | Común: `sessionId`, `exerciseType`, `processedVideoFilePath`, `videoFps`, `lowFpsWarning`, `fatigueIndex`. Sentadilla: `totalReps`, `validReps`, `reps[]`, `averageConcentricVelocity`, `estimatedRM`. Salto: `totalJumps`, `jumps[]`, `bestJumpHeightCm`, `avgJumpHeightCm`, `peakPowerW` |

`main.py` despacha según `exerciseType` a `SquatAnalyzer` o `JumpAnalyzer`.

> Python lee y escribe el vídeo **en el mismo disco que el backend**, a partir de la ruta que recibe. Ambos servicios tienen que correr en la misma máquina.

### Algoritmo de sentadilla (`services/squat_analyzer.py`)

1. **Lado del cuerpo**: elige izquierda o derecha según la visibilidad media de cadera, rodilla y tobillo; si es menor de 0,3, descarta el frame.
2. **Ángulos**: rodilla (cadera–rodilla–tobillo) y cadera (hombro–cadera–rodilla), suavizados con una media móvil exponencial (α = 0,2).
3. **Máquina de estados** sobre el ángulo de rodilla:

| Transición | Condición |
|---|---|
| `STANDING → DESCENDING` | rodilla < 150° |
| `DESCENDING → ASCENDING` | la rodilla sube más de 15° sobre el mínimo de la repetición |
| `ASCENDING → STANDING` | rodilla > 160°; cuenta la repetición si la subida duró ≥ 0,25 s |

4. **Profundidad** (`services/depth.py`): una repetición es válida si la cadera queda por debajo de la rodilla (`hip.y > knee.y` en coordenadas de imagen) durante al menos 2 frames consecutivos. Es una aproximación con los centros articulares.
5. **Métricas**:

| Métrica | Cálculo | Ojo |
|---|---|---|
| `concentricVelocity` | `1 / tiempo de subida` | Está en 1/s, no en m/s: solo sirve para comparar repeticiones entre sí |
| `estimatedRM` | Epley: `peso × (1 + 0,0333 × repeticiones válidas)` | Solo si se envía `weightKg` |
| `fatigueIndex` | `1 − v_última / v_primera`, con mínimo 0 | Necesita al menos 2 repeticiones |

### Algoritmo de salto vertical (`services/jump_analyzer.py` + `services/jump_metrics.py`)

`JumpAnalyzer` solo extrae una muestra por frame con MediaPipe: altura de los pies (media de talones y puntas visibles), altura de la cadera, ángulo de rodilla y longitud cadera–tobillo. Todo el cálculo vive en `jump_metrics.py`, que es puro y se testea con datos sintéticos.

1. **FPS**: se usan los del vídeo; si el fichero no los declara, se asumen **60**. Por debajo de 60, `lowFpsWarning = true`.
2. **Suelo**: mediana de la altura de los pies en todo el vídeo (la mayor parte del tiempo se está apoyado).
3. **Vuelo**: los pies están en el aire cuando suben más de un 5 % de la longitud de la pierna sobre el suelo durante al menos 0,1 s. Los frames sin pies detectados mantienen el estado anterior.
4. **Métricas por salto**:

| Métrica | Cálculo | Necesita |
|---|---|---|
| `flightTime` | (frame de aterrizaje − frame de despegue) / fps | — |
| `heightFlightCm` | `g·t² / 8` | — |
| `takeoffVelocity` | `g·t / 2` (m/s) | — |
| `minKneeAngle` | Mínimo en los 1,5 s previos al despegue | — |
| `countermovementDepthCm` | Bajada máxima de la cadera respecto a la posición de pie, en los 1,5 s previos | `heightCm` |
| `heightDisplacementCm` | Subida máxima de la cadera respecto a la posición de pie, durante el vuelo | `heightCm` |
| `peakPowerW` | Sayers: `60,7·h(cm) + 45,3·masa(kg) − 2055` | `bodyWeightKg` |

5. **Escala** (`heightCm`): la distancia cadera–tobillo es ≈ 0,491 de la estatura (Drillis y Contini), así que metros por píxel = `0,491 · altura / longitud de la pierna en píxeles`.
6. **Sesión**: mejor salto, altura media, potencia máxima y fatiga (`1 − h_último / h_primero`).

> La altura por desplazamiento incluye la extensión de tobillos en el despegue, así que suele salir algo mayor que la del tiempo de vuelo. Sirve para contrastar, no para sustituirla.

## App Android (`FEPerfectSquat`)

La interfaz sigue el diseño "PowerMetrics 1B" de Claude Design (tema oscuro, acento naranja, textos en inglés).

| Pieza | Fichero | Notas |
|---|---|---|
| Navegación | `ui/navigation/AppNavigation.kt` | Welcome → Profile la primera vez; después la app con pestañas Home / Analyze / Historic / Community (`ui/screens/AppShell.kt`) |
| Onboarding | `ui/screens/onboarding/`, `ui/viewmodels/ProfileViewModel.kt`, `data/ProfileRepository.kt` | Carrusel de 3 slides y perfil (nombre, edad, sexo, peso, altura, kg/lb) guardado en `SharedPreferences`. La altura no está en el diseño: el salto la necesita para la escala |
| Tema | `ui/theme/`, `ui/components/PmComponents.kt` | Tokens de color del diseño y componentes compartidos. Las fuentes Archivo y JetBrains Mono se sustituyen por sans y monospace del sistema |
| Home | `ui/screens/home/HomeScreen.kt`, `models/HomeStats.kt` | Mejores marcas de los últimos 30 días comparadas con las anteriores. La velocidad se muestra en 1/s (normalizada por el backend), no en m/s |
| Analyze | `ui/screens/analyze/`, `ui/viewmodels/MainViewModel.kt`, `models/ResultSummary.kt` | Crea la sesión, sube el vídeo con progreso, hace polling y descarga el vídeo con el esqueleto. El salto usa peso y altura del perfil; si el vídeo va por debajo de 60 fps se avisa al subirlo y en el resultado |
| Historic | `ui/screens/history/HistoryScreen.kt`, `ui/viewmodels/SessionsViewModel.kt` | `GET /session`: gráfico de las últimas 8 sesiones (1RM estimado o mejor salto) y lista de sesiones `FINISHED` |
| Community | `ui/screens/community/CommunityScreen.kt`, `data/CommunityRepository.kt`, `models/Community.kt` | **Datos de ejemplo** (`SampleCommunityRepository`) marcados en pantalla: el backend aún no tiene usuarios ni rankings |
| Red | `network/ApiService.kt`, `network/RetrofitClient.kt`, `network/ContentUriRequestBody.kt` | `BuildConfig.BASE_URL` por build type (por defecto `http://10.0.2.2:8080/`, emulador → localhost; se cambia con `-PdebugBaseUrl=` / `-PreleaseBaseUrl=`). El vídeo se sube en streaming desde el `ContentResolver` |
| FPS | `video/VideoFps.kt` | Lee los FPS del vídeo elegido con `MediaExtractor` |


## Cómo ejecutarlo en local

| Paso | Comando |
|---|---|
| 1. Servicio de análisis | `cd PYAnalysisService && py -3.12 -m venv .venv && .venv/Scripts/pip install -r requirements.txt && cd app && ../.venv/Scripts/python main.py` (Python 3.12: `mediapipe==0.10.14` solo publica wheels hasta 3.12; ver `.python-version`) |
| 2. Backend | `cd BEPerfectSquat && ./mvnw spring-boot:run` |
| 3. App | Abrir `FEPerfectSquat/` en Android Studio y ejecutar en el emulador |

## Tests

| Módulo | Comando | Notas |
|---|---|---|
| Backend | `cd BEPerfectSquat && ./mvnw test` | El `pom.xml` fija `-Dnet.bytebuddy.experimental=true` en el `argLine` para que Mockito funcione con JDK 25 |
| Python | `cd PYAnalysisService && .venv/Scripts/python -m unittest discover -s tests` | Lógica pura (`depth.py`, `jump_metrics.py`) y contrato (`models.py`); necesita `pydantic` en el venv, no MediaPipe; corren en Python 3.12 o 3.14 |
| App | Android Studio | Solo existen los tests de ejemplo |

## Convenciones de trabajo

- **Ramas**: `feat/...`, `fix/...` sobre `main`; nunca trabajar directamente en `main`.
- **Commits**: Conventional Commits (`feat(backend): ...`, `fix(analysis): ...`), un commit por unidad de trabajo, con sus tests.
- **Seguimiento de features grandes**: `odd/tasks/<feature>.md` (especificaciones, tareas y bitácora).
- **Idioma**: código e identificadores en inglés; documentación, comentarios y textos de la UI en español.
