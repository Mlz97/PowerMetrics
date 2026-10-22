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
- [x] T1 S1-S7 inline — theme tokens, shared components, pure domain helpers (units, jump math, stats) + tests — eee3e0c; RED observed (unresolved symbols), GREEN 20 tests; assess medium/under_budget
- [x] T2 S1,S2 inline — onboarding + profile persistence + nav gate — 6543096; no unit RED (SharedPreferences/Compose need a device; ProfileDraft rules covered by T1 tests); assembleDebug OK
- [x] T3 S3,S7 inline — app shell with tabs + Home — cbe4bfe; HomeStats tests from T1; assembleDebug OK
- [x] T4 S4 inline — Analyze flow — 101391c; RED ResultSummaryTest (unresolved) → GREEN 4/4; legacy Menu/Statistics/StartAnalysis/AnalysisResult/Main screens removed here
- [x] T5 S5 inline — Historic — 19d5203; historyChart covered by T1 tests; legacy HistoryScreen removed
- [x] T6 S6 inline — Community mock — 9642703; RED metric format test → GREEN
- [x] T7 S7 inline — dead SessionStats removed, jump fps pre-upload warning restored, docs/ARQUITECTURA.md updated — see commit below; 22/22 unit tests

## Log
- L1 (2026-10-22) User: "Vamos a darle caña al front con esto que he scado de claude design" + Claude Design import of `PowerMetrics Onboarding.dc.html` (canvas embedding `PowerMetrics 1B.dc.html`), "Implement: `PowerMetrics Onboarding.dc.html`".
- L2 (2026-10-22) Community has no backend → user chose "Mock marcado (Recomendado)".
- L3 Route: all tasks inline (no Writer trigger: sequential units sharing theme/components).
- L4 Decisions: UI copy follows the design (English; previous UI was Spanish). Fonts: system sans/mono stand in for Archivo/JetBrains Mono. Design's in-app gallery sheet → system video picker. "Discard" dropped (backend has no delete; every session is stored) → "Done" / "See in Historic". Tracked-video player kept as "Watch tracked video". A failed processed-video download no longer hides the metrics.
- L6 Independent verifier: S1-S3, S5-S7 PASS, S4 FAIL. Blockers fixed in 2f30f60: B1 upload % restarted (logging interceptor writes the multipart to an okio.Buffer first → progress ignored for Buffer sinks), B2 lb bodyweight hid 0.5 kg steps (formatBodyWeight, one decimal; RED→GREEN, 23 tests), B3 dark icons on light-mode phones (SystemBarStyle.dark). Verifier could not load DesignSync, so copy/colors were not compared against the design. Open advisories: no profile edit screen, Home has no loading/error state, system back exits from any tab, whole video still buffered in RAM by BODY logging (pre-existing), MOV sent as video/mp4.
- L5 Verification gap: emulator cannot boot (no Android hypervisor driver installed), so no on-device/visual check was possible; only unit tests + assembleDebug.
