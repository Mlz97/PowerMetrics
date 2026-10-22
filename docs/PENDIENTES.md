# Pendientes

Lista viva de lo que queda por resolver, ordenada por prioridad. Al cerrar un punto, márcalo y anota el commit o PR que lo resolvió.

> Contexto técnico de cada pieza: [ARQUITECTURA.md](ARQUITECTURA.md).

## Prioridad alta: bloquea o pone en riesgo el trabajo

- [x] **Publicar el historial sin vídeos.** `origin/main` reescrito sin `BEPerfectSquat/uploads/` (force push a `c13171d`); `backup/before-video-purge` borrada y `git gc --prune=now` ejecutado (`.git` de 46 MB a 1,2 MB).
- [ ] **Integrar `feat/depth-rule-async-analysis` en `main`** (PR o merge).
- [x] **Compilar la app** tras integrar `fix/pending-items`: `./gradlew assembleDebug testDebugUnitTest` en verde, incluido `SessionStatsTest` (3/3).
- [ ] **Prueba de punta a punta** con MediaPipe instalado: subir un vídeo real y comprobar que la sesión termina en `FINISHED` y la regla de profundidad da resultados razonables.
- [x] **Versión de Python del servicio.** `mediapipe==0.10.14` solo publica wheels para cp39–cp312, así que no se instala en 3.14. El servicio usa Python 3.12 (`PYAnalysisService/.python-version`); MediaPipe 1.x sí tiene wheel universal, pero no se ha comprobado que mantenga la API `mp.solutions` que usan los analizadores. (4db2fc8)
- [x] **Tests del backend en JDK 25.** `byte-buddy.version` fijado a 1.17.6 en el `pom.xml` (la 1.15.11 que trae Spring Boot 3.4 rechaza JDK 25); `./mvnw test` pasa sin flags. Sustituye al flag experimental en el `argLine` de `fix/pending-items`. (118b5f2)
- [x] **Integrar `fix/pending-items`** en `feat/vertical-jump` (merge).
- [ ] **Tráfico HTTP en claro en la app.** Con `targetSdk 36` Android bloquea `http://` por defecto y el manifiesto no declara `usesCleartextTraffic` ni `networkSecurityConfig`. La app compila, pero no se ha probado en un dispositivo. Si falla, permitir solo `10.0.2.2` en un `network_security_config.xml` de debug.

## Prioridad media: funcionalidad y calidad de las métricas

- [ ] **Persistencia real.** H2 en memoria pierde todas las sesiones al reiniciar, y los vídeos quedan huérfanos en disco. PostgreSQL ya está en el `pom.xml`.
- [x] **Histórico y Estadísticas.** `GET /session` lista las sesiones (la más reciente primero) y las dos pantallas lo usan; las estadísticas solo cuentan sesiones `FINISHED`. (884d3cf)
- [ ] **Velocidad real.** `concentricVelocity` es `1 / tiempo de subida` (1/s), no m/s. Para tener m/s hace falta una escala (altura del usuario, diámetro del disco...).
- [ ] **Calibrar la regla de profundidad** con vídeos reales: usa centros articulares de MediaPipe y no la cresta de la cadera ni la parte superior de la rodilla.
- [x] **Errores silenciados en Python.** El bucle de frames registra el primer fallo con traza y un resumen al final (`services/frame_errors.py`); `error_log.txt` se sustituye por `logger.exception`. (4db2fc8)
- [x] **Mensajes de error del backend.** Estado de sesión inválido → 409 `INVALID_STATE` con el estado actual; `IllegalArgumentException` devuelve su propio mensaje; `StorageException` → 500 `STORAGE_ERROR`; vídeo vacío → 400. (3c8ef1a)
- [ ] **Acoplamiento por disco.** Python recibe una ruta absoluta del backend, así que ambos deben compartir máquina. Valorar enviar el fichero o usar un almacenamiento común.

## Prioridad baja: limpieza

- [x] `weightKg.intValue()` trunca el peso (102,5 kg → 102): ahora se guarda y se devuelve como decimal. (77f359b)
- [x] `AnalysisSessionService` usa nombres de clase completos en vez de imports. (3c8ef1a)
- [x] `prepareVideoPart` carga el vídeo entero en memoria: ahora usa `ContentUriRequestBody` en streaming. (2406fa3)
- [x] `BASE_URL` fija a `10.0.2.2`: ahora es `BuildConfig.BASE_URL` por build type, sobrescribible con `-PdebugBaseUrl=` / `-PreleaseBaseUrl=`. (2406fa3)
- [x] Dos endpoints de subida: se elimina `POST /video`; queda `POST /session/{id}/video`. (77f359b)
- [x] `readTimeout` de 120 s en la app: baja a 30 s, las respuestas ya son inmediatas con el polling. (2406fa3)
- [x] `opencv-python` sin versión fijada: `opencv-python==4.10.0.84`. (4db2fc8)
- [x] `pydantic==2.6.1` sin wheel para Python 3.14: `pydantic==2.14.1`, que tiene wheels para 3.12 y 3.14 y funciona con `fastapi==0.109.2`. (4db2fc8)
- [x] La respuesta de sesión expone `reps[]` de la sentadilla y la app la muestra. (77f359b)
- [x] La app mostraba la velocidad concéntrica en "m/s": ahora "1/s". (2406fa3)
- [x] Carpetas `.idea/` versionadas: fuera del repositorio e ignoradas. (c4c4df2)
- [ ] `numpy` no está fijado y `mediapipe==0.10.14` es anterior a NumPy 2: si al instalar aparece "compiled using NumPy 1.x", fijar `numpy<2`.

## Salto vertical

Implementado en `feat/vertical-jump` (seguimiento: `odd/tasks/vertical-jump.md`). Queda:

- [x] **Compilar la app** con los cambios del salto (selector, campos, aviso de FPS, resultado): compila sin errores.
- [ ] **Calibrar con vídeos reales**: umbral de vuelo (5 % de la pierna), duración mínima (0,1 s) y ventana del contramovimiento (1,5 s).
- [ ] **Grabar desde la app a 60 fps**: hoy solo se elige un vídeo de la galería y se avisa si viene por debajo de 60 fps.
- [ ] **Salto en curso al final del vídeo**: si el vídeo termina en el aire, ese salto no se cuenta.
- [ ] Solo existe el CMJ; el salto sin contramovimiento (SJ) o el drop jump necesitarían su propio tipo.
