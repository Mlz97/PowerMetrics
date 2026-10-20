Objective: resolve the code-fixable items of `docs/PENDIENTES.md`; items that need the user (force-push, merge, Android build, real-video calibration, product decisions) stay open.
Branch: fix/pending-items (on feat/vertical-jump) · Delivery: ask-on-risk · Runners: `./mvnw test` (BEPerfectSquat, JDK 25), `py -m unittest discover -s tests` (PYAnalysisService, needs a venv with pydantic >= 2.11 on Python 3.14), no Android SDK locally.

## Specs

- S1 "Tests del backend en JDK 25. Hoy necesitan `-DargLine=-Dnet.bytebuddy.experimental=true`" → option "fijar el flag en el `argLine` de surefire en el `pom.xml`".
- S2 "`IllegalArgumentException` responde siempre "Invalid video data", aunque la causa sea el estado de la sesión." / "Las `RuntimeException` de `StorageService` no tienen handler y llegan como 500 genérico."
- S3 "`weightKg.intValue()` trunca el peso (102,5 kg → 102)."
- S4 "`AnalysisSessionService` usa nombres de clase completos en vez de imports."
- S5 "La respuesta de sesión no expone el detalle por repetición de la sentadilla (`reps[]`), aunque sí el de los saltos."
- S6 "Dos endpoints de subida: `POST /video` y `POST /session/{id}/video`; dejar uno." Assumption: keep `POST /session/{id}/video` (the one the app uses).
- S7 "`except Exception: pass` dentro del bucle de frames: registrar con `logger` en lugar de ignorar. Quitar `error_log.txt`."
- S8 "`opencv-python` sin versión fijada en `requirements.txt`." / "`pydantic==2.6.1` no tiene wheel para Python 3.14" / "verificar que `mediapipe==0.10.14` tiene wheels para esa versión o usar un venv con una versión compatible."
- S9 App: "`prepareVideoPart` carga el vídeo entero en memoria (`readBytes()`); usar un `RequestBody` en streaming." / "`BASE_URL` fija a `10.0.2.2` (solo emulador); moverla a `BuildConfig` por variante." / "`readTimeout` de 120 s en la app ya no hace falta con el polling." / "La app muestra la velocidad concéntrica en "m/s", pero el valor está en 1/s."
- S10 "Carpetas `.idea/` versionadas."
- S11 "Histórico y Estadísticas. Falta `GET /session` (listado) en el backend; las pantallas muestran valores fijos."

## Tasks

- T1 [S1] inline — pom.xml argLine — pending
- T2 [S2, S4] inline — backend error mapping + imports — pending
- T3 [S3, S5, S6] inline — weight as decimal, squat reps in response, single upload endpoint — pending
- T4 [S7, S8] inline — Python logging + dependency pins — pending
- T5 [S9] inline — app network/UI fixes — pending (not compilable locally)
- T6 [S10] inline — untrack .idea — pending
- T7 [S11] inline — session listing endpoint + history/statistics screens — pending
- T8 docs — PENDIENTES.md / ARQUITECTURA.md — pending

## Log

- L1 (2026-10-20) "Ve resolviendo las incidencias que hay en el archivo docs/ PENDIENTES.md"
- L2 Baseline at 9dddf8e: backend 32/32 green with `-DargLine=-Dnet.bytebuddy.experimental=true` on JDK 25.0.4.
