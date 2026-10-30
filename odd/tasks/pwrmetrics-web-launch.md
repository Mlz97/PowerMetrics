# PwrMetrics web launch

Objective: rename the project to PwrMetrics and prepare backend, analysis service, a new React web frontend and a Docker Compose deployment for a public launch without login.
Branch: `feat/pwrmetrics-web` (from `feat/powermetrics-redesign`). Delivery: ask-on-risk. Runners: `./mvnw test`, `pytest`, `npm test` (Vitest).
Approved plan: `~/.claude/plans/vale-finalmente-este-proyecto-se-serene-bonbon.md`.

## Specs

- S1 Rename: "este proyecto se va a llamar PwrMetrics". Visible names only (Spring app name, FastAPI title, docs, web); Java packages and folders stay.
- S2 "CORS permitiendo el dominio de PwrMetrics." Allowed origins come from `pwr.cors.allowed-origins`.
- S3 "Límite de tamaño de subida en Spring (max-file-size y max-request-size)." Oversized uploads answer 413 `FILE_TOO_LARGE`; only `.mp4 .mov .m4v .webm` are accepted (400 otherwise).
- S4 "Normalizar el vídeo con ffmpeg antes de analizarlo, para que los .mov/HEVC de iPhone no rompan el análisis." Also: max duration, and the annotated video is H.264 so browsers play it.
- S5 "Algún límite de peticiones por IP. Sin login, cualquiera puede subirte vídeos y gastarte CPU." Per-IP limits answer 429 with `Retry-After`; a full analysis queue answers 503 `ANALYSIS_BUSY`.
- S6 Anonymous owner (`pwr_cid` cookie): a client only sees, uploads to and downloads its own sessions (others → 404).
- S7 Production config: `prod` profile with PostgreSQL, H2 console off, storage dir from config, forwarded headers; retention job deletes video files older than `pwr.retention.days` (7).
- S8 Web frontend: React + Vite + TS with parity to the Android screens (Welcome/Profile, Home, Analyze, History, Community), upload progress, error messages for 413/429/503.
- S9 Deployment: Dockerfiles + `docker-compose.yml` (caddy, api, analysis, postgres) + Caddyfile + `.env.example` + docs.

## Tasks

| ID | Specs | Route | Status | Commit |
|----|-------|-------|--------|--------|
| T1 | S1, S7 | inline | pending | |
| T2 | S2, S3 | inline | pending | |
| T3 | S6 | inline | pending | |
| T4 | S5 | inline | pending | |
| T5 | S7 (retention, inline download) | inline | pending | |
| T6 | S4 | inline | pending | |
| T7 | S8 | inline | pending | |
| T8 | S9 | inline | pending | |

Route evidence: units run sequentially in one session; no writer trigger chosen (work inline by default).

## Log

- L1 (2026-10-30) User request: "Vale,finalmente este proyecto se va a llamar PwrMetrics y se va a migrar a una web. Necesito que planees los siguientes ajustes y consideres lo necesario para poder lanzarlo en web. — CORS permitiendo el dominio de PwrMetrics. / Límite de tamaño de subida en Spring (max-file-size y max-request-size). / Normalizar el vídeo con ffmpeg antes de analizarlo, para que los .mov/HEVC de iPhone no rompan el análisis. / Algún límite de peticiones por IP. Sin login, cualquiera puede subirte vídeos y gastarte CPU."
- L2 Decisions: web frontend included ("Backend + frontend web"), stack "React + Vite + TS", deploy "VPS + Docker Compose". Plan approved.
- L3 Environment: no ffmpeg and no Docker on the dev machine; ffmpeg integration tests skip without it and the compose stack cannot be run locally.
