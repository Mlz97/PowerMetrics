Objective: powerlifting depth rule, purge committed videos, asynchronous analysis request.
Branch: feat/depth-rule-async-analysis · Delivery: ask-on-risk · Runners: `py -m unittest` (PYAnalysisService), `mvnw test` (BEPerfectSquat), Android build unavailable locally (no SDK).

## Specs

- S1 "arregla la regla de profundidad": a rep is valid only when the hip breaks the parallel of the knee (README: "la cadera debe pasar la paralela de la rodilla"), replacing the knee-angle <= 90° check. Assumption: approximated with MediaPipe joint centers (hip y below knee y in image coordinates), held for at least 2 consecutive frames to filter landmark jitter.
- S2 "borra el commit de los vídeos y añade uploads al gitignore": remove `BEPerfectSquat/uploads` from history keeping the rest of commit bc6cc7b; ignore `uploads/`; keep the local files on disk.
- S3 "Modifica correctamente la petición del análisis": `POST /session/{id}/video` stores the video and returns 202 with the session in PROCESSING; analysis runs in the background and sets FINISHED or ERROR; the app polls `GET /session/{id}` until it finishes, then downloads the processed video.

## Tasks

- T1 [S2] inline — history purge (local, backup branch `backup/before-video-purge`) + `.gitignore` — done
- T2 [S1] inline — depth rule in PYAnalysisService with unit tests — pending
- T3 [S3] inline — backend async analysis + tests — pending
- T4 [S3] inline — app polling — pending

## Log

- L1 (2026-10-13) "Vale sabiendo esto, quiero que arregles la regla de profundidad, borra el commit de los vídeos y añade uploads al gitignore. Modifica correctamente la petición del análisis. Cuando tengas esto seguimos trabajando"
- L2 Commit bc6cc7b also carried app/Python code, so only `BEPerfectSquat/uploads` was filtered out (git filter-branch --index-filter, bc6cc7b^..main). Non-upload trees verified identical. origin/main still has the videos: a force push is the user's decision.
