# Pendientes

Lista viva de lo que queda por resolver, ordenada por prioridad. Al cerrar un punto, márcalo y anota el commit o PR que lo resolvió.

> Contexto técnico de cada pieza: [ARQUITECTURA.md](ARQUITECTURA.md).

## Prioridad alta: bloquea o pone en riesgo el trabajo

- [x] **Publicar el historial sin vídeos.** `origin/main` reescrito sin `BEPerfectSquat/uploads/` (force push a `c13171d`); `backup/before-video-purge` borrada y `git gc --prune=now` ejecutado (`.git` de 46 MB a 1,2 MB).
- [ ] **Integrar `feat/depth-rule-async-analysis` en `main`** (PR o merge).
- [ ] **Compilar la app** en Android Studio: el polling de `MainViewModel` no se ha compilado (no hay SDK de Android en el equipo donde se escribió).
- [ ] **Prueba de punta a punta** con MediaPipe instalado: subir un vídeo real y comprobar que la sesión termina en `FINISHED` y la regla de profundidad da resultados razonables.
- [ ] **Versión de Python del servicio.** El equipo tiene Python 3.14; verificar que `mediapipe==0.10.14` tiene wheels para esa versión o usar un venv con una versión compatible.
- [ ] **Tests del backend en JDK 25.** Hoy necesitan `-DargLine=-Dnet.bytebuddy.experimental=true`. Opciones:
  - usar JDK 17 o 21, que es lo que pide el proyecto;
  - subir Spring Boot o Mockito a versiones que soporten JDK 25;
  - fijar el flag en el `argLine` de surefire en el `pom.xml`.

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

## Salto vertical

Implementado en `feat/vertical-jump` (seguimiento: `odd/tasks/vertical-jump.md`). Queda:

- [ ] **Compilar la app** con los cambios del salto (selector, campos, aviso de FPS, resultado).
- [ ] **Calibrar con vídeos reales**: umbral de vuelo (5 % de la pierna), duración mínima (0,1 s) y ventana del contramovimiento (1,5 s).
- [ ] **Grabar desde la app a 60 fps**: hoy solo se elige un vídeo de la galería y se avisa si viene por debajo de 60 fps.
- [ ] **Salto en curso al final del vídeo**: si el vídeo termina en el aire, ese salto no se cuenta.
- [ ] Solo existe el CMJ; el salto sin contramovimiento (SJ) o el drop jump necesitarían su propio tipo.
