Objective: add vertical jump (countermovement jump) as a second exercise next to the squat: endpoints, analysis parameters and metrics.
Branch: feat/vertical-jump (stacked on feat/depth-rule-async-analysis) · Delivery: ask-on-risk (forecast ~1100 authored lines; chain strategy to be asked before opening PRs) · Runners: `py -m unittest discover -s tests` (PYAnalysisService), `./mvnw test -DargLine=-Dnet.bytebuddy.experimental=true` (BEPerfectSquat), no Android SDK locally.

## Specs

- S1 Exercise type on the session: `POST /session` accepts an optional body `{ "exerciseType": "SQUAT" | "VERTICAL_JUMP" }`, default `SQUAT` so the current app keeps working. `GET /session/{id}` returns `exerciseType`.
- S2 Jump analysis parameters on upload: `POST /session/{id}/video` accepts `bodyWeightKg` (for power) and `heightCm` (user height, "pedir la altura al usuario para hacer cálculos"), besides the squat `weightKg`. Out-of-range values return 400.
- S3 Python `POST /analyze` receives `exerciseType`, `bodyWeightKg`, `heightCm` and dispatches to the squat or the jump analyzer.
- S4 Jump metrics per jump: flight time, height from flight time (`h = g·t²/8`), takeoff velocity (`v = g·t/2`), countermovement depth (min knee angle before takeoff), peak power (Sayers, needs body weight). With `heightCm`: pixel→meter scale from leg length, giving countermovement depth in cm and height from hip displacement. Session summary: total jumps, best and average height, best peak power, fatigue (height drop first→last).
- S5 FPS: "vamos a poner el aviso y que por defecto sea a 60 fps". Assumption (app picks videos from the gallery, it does not record): the analyzer uses the real video FPS and falls back to 60 when unknown; the response carries `videoFps` and `lowFpsWarning` (< 60); the app warns before uploading a jump video and on the result when FPS < 60.
- S6 App: exercise selector, required body weight and height fields for the jump, jump metrics on the result screen.

## Tasks

- T1 [S3, S4, S5] inline — Python: pure jump metrics module + JumpAnalyzer + request/response contract — done (674019b), RED (module missing) -> GREEN 29/29 (pydantic 2.14 venv)
- T2 [S1, S2, S3, S5] inline — Backend: ExerciseType, parameters, JumpDetail, client contract, response DTO + tests — done (d0118a3), RED (missing symbols) -> GREEN 32/32
- T3 [S1, S2, S5, S6] inline — App: selector, fields, FPS warning, jump result — done (4eca7af), NOT compiled: no Android SDK locally
- T4 docs — ARQUITECTURA.md and PENDIENTES.md updated — done

## Log

- L1 (2026-10-13) "Vale hazme un md con las cosas que van quedando pendientes para poder atenderlas, hazme también otro con la info de la arquitectura y todo lo necesario para consultar las bases del desarrollo. De cara a futuro quiero implementar también salto vertical además de sentadilla, así que podemos ir trabajando en eso. Preparando los endpoints y los parámetros del análisis."
- L2 (2026-10-13) Proposal accepted with changes: "Vale, vamos a poner el aviso y que por defecto sea a 60 fps. Además de pedir la altura al usuario para hacer cálculos"
- L3 Anthropometric scale: hip (greater trochanter) height ≈ 0.530·H and ankle height ≈ 0.039·H (Drillis & Contini), so hip→ankle ≈ 0.491·H. Measured in pixels on standing frames.
- L4 Own test error caught before GREEN: expected Sayers power was 3456.1 W, correct value for h = 30.656 cm and 80 kg is 3429.8 W.
- L5 Pre-existing FPS fallback for the squat stays at 30; only the jump uses the 60 fps default (S5 scope).
- L6 Pending: Android Studio build, calibration with real jump videos, PR chain strategy (forecast > 400 lines) before opening PRs.
