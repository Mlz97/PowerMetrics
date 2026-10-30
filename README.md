# PwrMetrics

Análisis de sentadilla y salto vertical a partir de un vídeo, con visión artificial. Subes un vídeo de tu serie y PwrMetrics mide cada repetición: velocidad, pérdida de velocidad, 1RM estimado y profundidad en la sentadilla; altura, tiempo de vuelo y potencia en el salto.

## Piezas

| Carpeta | Qué es |
|---|---|
| `web/` | Web en React + Vite + TypeScript |
| `BEPerfectSquat/` | Backend en Spring Boot: sesiones, subida de vídeos, límites de uso |
| `PYAnalysisService/` | Servicio de análisis en FastAPI con MediaPipe, OpenCV y ffmpeg |
| `deploy/` | Docker Compose y Caddy para un VPS |
| `FEPerfectSquat/` | App Android original (congelada) |

## Documentación

- [Arquitectura](docs/ARQUITECTURA.md): cómo encajan las piezas, endpoints, límites, cómo ejecutarlo en local y los tests.
- [Despliegue](docs/DESPLIEGUE.md): puesta en marcha en un VPS.
- [Pendientes](docs/PENDIENTES.md): lo que falta por resolver.

## Criterios

- Una sesión es un vídeo con una serie.
- Una repetición de sentadilla es válida si la cadera baja de la paralela de la rodilla (reglamento de powerlifting). Las inválidas se analizan, pero no suman al conteo.
- Sin cuentas de usuario: cada navegador ve solo sus sesiones (cookie anónima) y los vídeos se borran a los 7 días.
