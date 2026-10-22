# Pendientes

Lista viva de lo que queda por resolver, ordenada por prioridad. Al cerrar un punto, márcalo y anota el commit o PR que lo resolvió.

> Contexto técnico de cada pieza: [ARQUITECTURA.md](ARQUITECTURA.md).

## Prioridad alta: bloquea o pone en riesgo el trabajo

- [x] **Publicar el historial sin vídeos.** `origin/main` reescrito sin `BEPerfectSquat/uploads/` (force push a `c13171d`); `backup/before-video-purge` borrada y `git gc --prune=now` ejecutado (`.git` de 46 MB a 1,2 MB).
- [ ] **Integrar `feat/depth-rule-async-analysis` en `main`** (PR o merge).
- [x] **Compilar la app**: `./gradlew assembleDebug testDebugUnitTest` en verde sobre `feat/vertical-jump` (incluye el polling de `MainViewModel`).
- [ ] **Prueba de punta a punta** con MediaPipe instalado: subir un vídeo real y comprobar que la sesión termina en `FINISHED` y la regla de profundidad da resultados razonables.
- [ ] **Versión de Python del servicio.** El equipo tiene Python 3.14; verificar que `mediapipe==0.10.14` tiene wheels para esa versión o usar un venv con una versión compatible.
- [x] **Tests del backend en JDK 25.** `byte-buddy.version` fijado a 1.17.6 en el `pom.xml` (la 1.15.11 que trae Spring Boot 3.4 rechaza JDK 25); `./mvnw test` pasa sin flags.

- [ ] **Integrar `fix/pending-items`.** La rama sale de `feat/vertical-jump` en `9dddf8e` y no incluye los commits posteriores. Al integrarla:
  - JDK 25 está resuelto de dos formas: `fix/pending-items` usa el flag experimental en el `argLine` (`e7e73df`) y esta rama fija `byte-buddy` 1.17.6 (`118b5f2`). Quedarse con una sola; la recomendada es la de `byte-buddy`, porque no depende de un modo experimental.
  - `PENDIENTES.md` va a entrar en conflicto: combinar los puntos cerrados de ambas ramas.
  - La compilación de la app verificada aquí no incluye sus cambios en la app: subida en streaming, `BuildConfig.BASE_URL`, Histórico, Estadísticas y el test `SessionStatsTest`. Hay que volver a compilar.
- [ ] **Tráfico HTTP en claro en la app** (detectado en `fix/pending-items`). Con `targetSdk 36` Android bloquea `http://` por defecto, y el manifiesto no declara `usesCleartextTraffic` ni `networkSecurityConfig`. La app compila, pero no se ha probado en un dispositivo. Si falla, permitir solo `10.0.2.2` en un `network_security_config.xml` de debug.

## Prioridad media: funcionalidad y calidad de las métricas

- [ ] **Persistencia real.** H2 en memoria pierde todas las sesiones al reiniciar, y los vídeos quedan huérfanos en disco. PostgreSQL ya está en el `pom.xml`.
- [ ] **Histórico y Estadísticas.** Falta `GET /session` (listado) en el backend; las pantallas muestran valores fijos.
- [ ] **Velocidad real.** `concentricVelocity` es `1 / tiempo de subida` (1/s), no m/s. Para tener m/s hace falta una escala (altura del usuario, diámetro del disco...).
- [ ] **Calibrar la regla de profundidad** con vídeos reales: usa centros articulares de MediaPipe y no la cresta de la cadera ni la parte superior de la rodilla.
- [ ] **Errores silenciados en Python.** `except Exception: pass` dentro del bucle de frames: registrar con `logger` en lugar de ignorar. Quitar `error_log.txt`.
- [ ] **Mensajes de error del backend.**
  - `IllegalArgumentException` responde siempre "Invalid video data", aunque la causa sea el estado de la sesión.
  - Las `RuntimeException` de `StorageService` no tienen handler y llegan como 500 genérico.
- [ ] **Acoplamiento por disco.** Python recibe una ruta absoluta del backend, así que ambos deben compartir máquina. Valorar enviar el fichero o usar un almacenamiento común.

## Prioridad baja: limpieza

- [ ] `weightKg.intValue()` trunca el peso (102,5 kg → 102).
- [ ] `AnalysisSessionService` usa nombres de clase completos en vez de imports.
- [ ] `prepareVideoPart` carga el vídeo entero en memoria (`readBytes()`); usar un `RequestBody` en streaming.
- [ ] `BASE_URL` fija a `10.0.2.2` (solo emulador); moverla a `BuildConfig` por variante.
- [ ] Dos endpoints de subida: `POST /video` y `POST /session/{id}/video`; dejar uno.
- [ ] `readTimeout` de 120 s en la app ya no hace falta con el polling.
- [ ] `opencv-python` sin versión fijada en `requirements.txt`.
- [ ] `pydantic==2.6.1` no tiene wheel para Python 3.14: los tests se pasaron con pydantic 2.14 en un venv local.
- [ ] La respuesta de sesión no expone el detalle por repetición de la sentadilla (`reps[]`), aunque sí el de los saltos.
- [ ] La app muestra la velocidad concéntrica en "m/s", pero el valor está en 1/s.
- [ ] Carpetas `.idea/` versionadas.

- [ ] `numpy` no está fijado y `mediapipe==0.10.14` es anterior a NumPy 2: si al instalar aparece "compiled using NumPy 1.x", fijar `numpy<2` (detectado en `fix/pending-items`).

## Resuelto en `fix/pending-items`, pendiente de integrar

Estos puntos de la lista ya están resueltos en esa rama. Se marcarán como cerrados aquí cuando se integre.

| Punto | Commit |
|---|---|
| Versión de Python: el servicio usa Python 3.12 (`.python-version`), porque `mediapipe==0.10.14` solo publica wheels hasta cp312. No se ha comprobado que MediaPipe 1.x mantenga la API `mp.solutions`. | `4db2fc8`, `ac048d6` |
| Errores silenciados en Python: se registra el primer fallo con su traza y un resumen; fuera `error_log.txt`. | `4db2fc8` |
| `opencv-python==4.10.0.84` y `pydantic==2.14.1` fijados. | `4db2fc8` |
| Mensajes de error del backend: 409 `INVALID_STATE`, mensaje real de `IllegalArgumentException`, 500 `STORAGE_ERROR`. | `3c8ef1a` |
| Imports en `AnalysisSessionService`. | `3c8ef1a` |
| Peso decimal, `reps[]` de la sentadilla en la respuesta y eliminación de `POST /video`. | `77f359b` |
| App: subida en streaming, `BuildConfig.BASE_URL` por variante, `readTimeout` de 30 s y unidad "1/s". | `2406fa3` |
| `.idea/` fuera del repositorio. | `c4c4df2` |
| Histórico y Estadísticas con `GET /session`. | `884d3cf` |

## Salto vertical

Implementado en `feat/vertical-jump` (seguimiento: `odd/tasks/vertical-jump.md`). Queda:

- [x] **Compilar la app** con los cambios del salto (selector, campos, aviso de FPS, resultado): compila sin errores.
- [ ] **Calibrar con vídeos reales**: umbral de vuelo (5 % de la pierna), duración mínima (0,1 s) y ventana del contramovimiento (1,5 s).
- [ ] **Grabar desde la app a 60 fps**: hoy solo se elige un vídeo de la galería y se avisa si viene por debajo de 60 fps.
- [ ] **Salto en curso al final del vídeo**: si el vídeo termina en el aire, ese salto no se cuenta.
- [ ] Solo existe el CMJ; el salto sin contramovimiento (SJ) o el drop jump necesitarían su propio tipo.
