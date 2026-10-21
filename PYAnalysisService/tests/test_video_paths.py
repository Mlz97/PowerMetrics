import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "app"))

from services.video_paths import analyzed_output_path  # noqa: E402


class AnalyzedOutputPathTest(unittest.TestCase):

    def test_mp4_keeps_previous_naming(self):
        self.assertEqual(analyzed_output_path("/uploads/video.mp4"), "/uploads/video_analyzed.mp4")

    def test_other_extensions_never_overwrite_the_input(self):
        for source in ("/uploads/video.mov", "/uploads/video.MP4", "/uploads/video"):
            with self.subTest(source=source):
                output = analyzed_output_path(source)
                self.assertNotEqual(output, source)
                self.assertTrue(output.endswith("_analyzed.mp4"))

    def test_only_the_file_extension_is_replaced(self):
        self.assertEqual(analyzed_output_path("/data.mp4/clip.mov"), "/data.mp4/clip_analyzed.mp4")


if __name__ == "__main__":
    unittest.main()
