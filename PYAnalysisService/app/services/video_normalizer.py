"""Normaliza los videos con ffmpeg antes del analisis y deja el anotado reproducible en el navegador.

Los .mov/HEVC de iPhone, la rotacion por metadatos y la tasa de frames variable rompen a OpenCV:
se convierten a H.264 a FPS constante (sin bajar los FPS, el salto los necesita) y ya rotados.
"""
import json
import logging
import os
import shutil
import subprocess
from contextlib import contextmanager
from dataclasses import dataclass
from typing import Iterator, List, Optional

logger = logging.getLogger(__name__)

MAX_VIDEO_SECONDS = float(os.getenv("PWR_MAX_VIDEO_SECONDS", "60"))
# En Docker es obligatorio; en local sin ffmpeg se analiza el original con un aviso
REQUIRE_FFMPEG = os.getenv("PWR_REQUIRE_FFMPEG", "false").lower() in ("1", "true", "yes")
FFMPEG_TIMEOUT_SECONDS = 300
MAX_SHORT_SIDE = 1080
MAX_FPS = 240.0


class VideoRejected(ValueError):
    """El video no se puede analizar: el mensaje se muestra al usuario."""


@dataclass(frozen=True)
class ProbeResult:
    has_video: bool
    duration: Optional[float]
    fps: Optional[float]


def fps_from_rate(rate: Optional[str]) -> Optional[float]:
    """Convierte una tasa de ffprobe ("60000/1001") en FPS; None si no es valida."""
    if not rate:
        return None
    try:
        num, _, den = rate.partition("/")
        value = float(num) / float(den or 1)
    except (ValueError, ZeroDivisionError):
        return None
    return value if 0 < value <= MAX_FPS else None


def parse_probe(data: dict) -> ProbeResult:
    streams = data.get("streams") or []
    if not streams:
        return ProbeResult(has_video=False, duration=None, fps=None)
    stream = streams[0]
    # avg_frame_rate es la tasa real en videos VFR; r_frame_rate puede ser la base de tiempo (600/1 en iPhone)
    fps = fps_from_rate(stream.get("avg_frame_rate")) or fps_from_rate(stream.get("r_frame_rate"))
    try:
        duration = float(data.get("format", {}).get("duration"))
    except (TypeError, ValueError):
        duration = None
    return ProbeResult(has_video=True, duration=duration, fps=fps)


def check_probe(probe: ProbeResult, max_seconds: float) -> None:
    if not probe.has_video:
        raise VideoRejected("El archivo no contiene vídeo")
    if probe.duration is not None and probe.duration > max_seconds:
        raise VideoRejected(f"El vídeo dura {probe.duration:.0f} s; el máximo es {max_seconds:.0f} s")


def normalized_path(video_path: str) -> str:
    base, _ = os.path.splitext(video_path)
    return f"{base}_normalized.mp4"


def _seconds(value: float) -> str:
    """Duracion en punto fijo: ffmpeg no acepta notacion exponencial (1e+06)."""
    return f"{value:.3f}".rstrip("0").rstrip(".")


def normalize_command(src: str, dst: str, fps: Optional[float], max_seconds: float) -> List[str]:
    # ffmpeg aplica la rotacion de los metadatos al decodificar, el resultado ya sale derecho
    command = ["ffmpeg", "-nostdin", "-y", "-v", "error", "-i", src, "-map", "0:v:0",
               # Se limita el lado corto: un vertical de movil (1080x1920 ya rotado) conserva su resolucion
               "-vf", f"scale='if(gt(iw,ih),-2,min({MAX_SHORT_SIDE},iw))':'if(gt(iw,ih),min({MAX_SHORT_SIDE},ih),-2)'"]
    if fps:
        command += ["-fps_mode", "cfr", "-r", f"{fps:.3f}"]
    # -t: un archivo sin duracion en la cabecera pasa ffprobe, pero no se procesa mas alla del limite
    command += ["-t", _seconds(max_seconds), "-c:v", "libx264", "-preset", "veryfast", "-crf", "23", "-pix_fmt", "yuv420p",
                "-an", dst]
    return command


def playable_command(src: str, dst: str) -> List[str]:
    return ["ffmpeg", "-nostdin", "-y", "-v", "error", "-i", src,
            "-c:v", "libx264", "-preset", "veryfast", "-crf", "23", "-pix_fmt", "yuv420p",
            "-movflags", "+faststart", "-an", dst]


def ffmpeg_available() -> bool:
    return shutil.which("ffmpeg") is not None and shutil.which("ffprobe") is not None


def _run(command: List[str]) -> subprocess.CompletedProcess:
    try:
        return subprocess.run(command, check=True, capture_output=True, text=True, timeout=FFMPEG_TIMEOUT_SECONDS)
    except subprocess.TimeoutExpired as e:
        raise RuntimeError(f"{command[0]} tardó más de {FFMPEG_TIMEOUT_SECONDS} s") from e
    except subprocess.CalledProcessError as e:
        raise RuntimeError(f"{command[0]} falló: {e.stderr.strip()[-500:]}") from e


def probe(video_path: str) -> ProbeResult:
    result = _run(["ffprobe", "-v", "error", "-select_streams", "v:0",
                   "-show_entries", "stream=avg_frame_rate,r_frame_rate:format=duration",
                   "-of", "json", video_path])
    return parse_probe(json.loads(result.stdout or "{}"))


@contextmanager
def normalized_video(video_path: str, max_seconds: float = MAX_VIDEO_SECONDS) -> Iterator[str]:
    """Entrega la ruta de una copia normalizada del video y la borra al terminar."""
    if not ffmpeg_available():
        if REQUIRE_FFMPEG:
            raise RuntimeError("ffmpeg/ffprobe no están instalados")
        logger.warning("ffmpeg not installed: analyzing %s without normalization", video_path)
        yield video_path
        return

    info = probe(video_path)
    check_probe(info, max_seconds)
    output = normalized_path(video_path)
    try:
        _run(normalize_command(video_path, output, info.fps, max_seconds))
        yield output
    finally:
        if os.path.exists(output):
            os.remove(output)


def make_browser_playable(video_path: str) -> None:
    """Recodifica el video anotado (mp4v de OpenCV) a H.264 con faststart, en la misma ruta."""
    if not ffmpeg_available():
        if REQUIRE_FFMPEG:
            raise RuntimeError("ffmpeg/ffprobe no están instalados")
        logger.warning("ffmpeg not installed: %s stays as mp4v and browsers may not play it", video_path)
        return
    if not os.path.exists(video_path):
        # OpenCV no llego a escribir el anotado: las metricas se devuelven igual
        logger.warning("Tracked video %s was not written", video_path)
        return
    base, _ = os.path.splitext(video_path)
    raw = f"{base}.raw.mp4"
    os.replace(video_path, raw)
    try:
        _run(playable_command(raw, video_path))
    except RuntimeError:
        # El video anotado es un extra: si falla la recodificacion se conserva el mp4v y las metricas siguen
        logger.exception("Could not make %s browser playable", video_path)
        os.replace(raw, video_path)
        return
    os.remove(raw)
