import os
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "app"))

from services.video_normalizer import (  # noqa: E402
    ProbeResult,
    VideoRejected,
    check_probe,
    fps_from_rate,
    normalize_command,
    normalized_path,
    parse_probe,
    playable_command,
)

HAS_FFMPEG = shutil.which("ffmpeg") is not None and shutil.which("ffprobe") is not None


class FpsFromRateTest(unittest.TestCase):

    def test_fraction(self):
        self.assertAlmostEqual(fps_from_rate("60000/1001"), 59.94, places=2)

    def test_integer_fraction(self):
        self.assertEqual(fps_from_rate("30/1"), 30.0)

    def test_invalid_rates_are_none(self):
        for rate in ("0/0", "", None, "abc", "30/0"):
            with self.subTest(rate=rate):
                self.assertIsNone(fps_from_rate(rate))


class ParseProbeTest(unittest.TestCase):

    def test_prefers_average_frame_rate_for_variable_frame_rate_iphone_videos(self):
        probe = parse_probe({
            "streams": [{"avg_frame_rate": "59.94/1", "r_frame_rate": "600/1"}],
            "format": {"duration": "12.5"},
        })
        self.assertEqual(probe, ProbeResult(has_video=True, duration=12.5, fps=59.94))

    def test_falls_back_to_real_frame_rate(self):
        probe = parse_probe({
            "streams": [{"avg_frame_rate": "0/0", "r_frame_rate": "30/1"}],
            "format": {"duration": "3"},
        })
        self.assertEqual(probe.fps, 30.0)

    def test_without_video_stream(self):
        probe = parse_probe({"streams": [], "format": {"duration": "3"}})
        self.assertFalse(probe.has_video)

    def test_missing_duration(self):
        probe = parse_probe({"streams": [{"avg_frame_rate": "30/1"}], "format": {}})
        self.assertIsNone(probe.duration)


class CheckProbeTest(unittest.TestCase):

    def test_accepts_short_video(self):
        check_probe(ProbeResult(has_video=True, duration=20.0, fps=60.0), max_seconds=60)

    def test_rejects_file_without_video(self):
        with self.assertRaises(VideoRejected) as ctx:
            check_probe(ProbeResult(has_video=False, duration=5.0, fps=None), max_seconds=60)
        self.assertEqual(str(ctx.exception), "El archivo no contiene vídeo")

    def test_rejects_too_long_video(self):
        with self.assertRaises(VideoRejected) as ctx:
            check_probe(ProbeResult(has_video=True, duration=75.2, fps=30.0), max_seconds=60)
        self.assertEqual(str(ctx.exception), "El vídeo dura 75 s; el máximo es 60 s")


class CommandTest(unittest.TestCase):

    def test_normalize_command_converts_to_constant_frame_rate_h264_without_audio(self):
        command = normalize_command("/videos/a.mov", "/videos/a_normalized.mp4", fps=59.94, max_seconds=60)
        self.assertEqual(command[0], "ffmpeg")
        self.assertIn("-nostdin", command)
        self.assertEqual(command[command.index("-i") + 1], "/videos/a.mov")
        self.assertEqual(command[command.index("-c:v") + 1], "libx264")
        self.assertEqual(command[command.index("-pix_fmt") + 1], "yuv420p")
        self.assertEqual(command[command.index("-fps_mode") + 1], "cfr")
        self.assertEqual(command[command.index("-r") + 1], "59.940")
        self.assertIn("-an", command)
        # Files without a duration in the header pass ffprobe: ffmpeg still stops at the limit
        self.assertEqual(command[command.index("-t") + 1], "60")
        self.assertEqual(command[-1], "/videos/a_normalized.mp4")

    def test_normalize_command_without_known_fps_keeps_source_timing(self):
        command = normalize_command("/videos/a.mov", "/videos/a_normalized.mp4", fps=None, max_seconds=60)
        self.assertNotIn("-r", command)

    def test_playable_command_uses_faststart_h264(self):
        command = playable_command("/videos/a_analyzed.raw.mp4", "/videos/a_analyzed.mp4")
        self.assertEqual(command[command.index("-c:v") + 1], "libx264")
        self.assertEqual(command[command.index("-movflags") + 1], "+faststart")
        self.assertEqual(command[-1], "/videos/a_analyzed.mp4")

    def test_normalized_path_never_overwrites_the_input(self):
        for source in ("/videos/a.mp4", "/videos/a.MOV", "/videos/a"):
            with self.subTest(source=source):
                output = normalized_path(source)
                self.assertNotEqual(output, source)
                self.assertTrue(output.endswith("_normalized.mp4"))


@unittest.skipUnless(HAS_FFMPEG, "ffmpeg/ffprobe not installed")
class NormalizeWithFfmpegTest(unittest.TestCase):

    def setUp(self):
        self.tmp = tempfile.mkdtemp()

    def tearDown(self):
        shutil.rmtree(self.tmp, ignore_errors=True)

    def _make_video(self, name, seconds, extra):
        path = os.path.join(self.tmp, name)
        subprocess.run(
            ["ffmpeg", "-v", "error", "-y", "-f", "lavfi", "-i", f"testsrc=size=320x240:rate=60:duration={seconds}",
             *extra, path],
            check=True)
        return path

    def test_hevc_mov_with_rotation_becomes_opencv_readable_h264(self):
        import cv2
        from services.video_normalizer import normalized_video

        source = self._make_video("iphone.mov", 2, ["-c:v", "libx265", "-tag:v", "hvc1",
                                                     "-metadata:s:v", "rotate=90"])
        with normalized_video(source, max_seconds=60) as normalized:
            cap = cv2.VideoCapture(normalized)
            self.assertTrue(cap.isOpened())
            self.assertAlmostEqual(cap.get(cv2.CAP_PROP_FPS), 60.0, places=0)
            ok, _ = cap.read()
            cap.release()
            self.assertTrue(ok)
        self.assertFalse(os.path.exists(normalized))

    def test_too_long_video_is_rejected(self):
        from services.video_normalizer import normalized_video

        source = self._make_video("long.mp4", 3, ["-c:v", "libx264"])
        with self.assertRaises(VideoRejected):
            with normalized_video(source, max_seconds=1):
                pass


if __name__ == "__main__":
    unittest.main()
