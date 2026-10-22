# PowerMetrics redesign (Claude Design "PowerMetrics 1B")
Objective: implement the dark 1B design in the Android app (FEPerfectSquat, Jetpack Compose). Branch: `feat/powermetrics-redesign` (from `main`).
Delivery: ask-on-risk (forecast ~1800 authored lines; chain strategy asked before opening PRs). Test runner: `./gradlew testDebugUnitTest` in `FEPerfectSquat/`.

## Specs
- S1 Welcome carousel, 3 slides "Squat → Vertical jump → Lift together", swipe + dots + `Skip` + `Next`/`Get started`, copy verbatim from the design (`SLIDES`).
- S2 Profile "Let's get to know you": First name, Age stepper (13–99), Units kg/lb, Sex (Male/Female/Other), Bodyweight stepper (35–200, 0.5 step). `Continue` enabled only with name and sex. Persisted on device; onboarding is skipped once a profile exists. Assumption: a Height stepper is added because vertical jump analysis requires `heightCm`.
- S3 Home: "Ready to train, {name}?", orange "Analyze a video" card, "Your stats" panel with Squat/Jump toggle ("Best in the last 30 days"), Historic and Community PRs cards. Stats come from real backend sessions; backend has no squat power and reports velocity in 1/s, so tiles show what the backend measures (assumption).
- S4 Analyze: Squat/Vertical jump segment, "Load on the bar" stepper (squat), "Upload a video" from the gallery, processing card with stage + %, "FILMING TIPS", result sheet with per-rep/jump bars and 3 stats. Uses the existing backend flow (create session → upload → poll → download processed video). Backend persists every session, so the sheet offers `Done` / `Save to Historic` → open Historic (no discard API).
- S5 Historic: Squat/Vertical jump pills, chart of the last 8 sessions (Estimated 1RM / Best jump height), session rows (day, month, lift, top set, metric).
- S6 Community PRs: Squat/Vertical jump segment, "RANK BY" metric chips, board + "You" row. User decision (2026-10-22): "Mock marcado" — design sample data behind a repository interface, clearly labeled as sample data.
- S7 Bottom navigation Home / Analyze / Historic / Community; old Menu/Statistics/StartAnalysis/AnalysisResult screens replaced.

## Tasks
- [ ] T1 S1-S7 inline — theme tokens, shared components, pure domain helpers (units, jump math, stats) + tests
- [ ] T2 S1,S2 inline — onboarding + profile persistence + nav gate
- [ ] T3 S3,S7 inline — app shell with tabs + Home
- [ ] T4 S4 inline — Analyze flow
- [ ] T5 S5 inline — Historic
- [ ] T6 S6 inline — Community mock
- [ ] T7 S7 inline — remove replaced screens, docs

## Log
- L1 (2026-10-22) User: "Vamos a darle caña al front con esto que he scado de claude design" + Claude Design import of `PowerMetrics Onboarding.dc.html` (canvas embedding `PowerMetrics 1B.dc.html`), "Implement: `PowerMetrics Onboarding.dc.html`".
- L2 (2026-10-22) Community has no backend → user chose "Mock marcado (Recomendado)".
- L3 Route: all tasks inline (no Writer trigger: sequential units sharing theme/components).
