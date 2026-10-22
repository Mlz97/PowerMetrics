import sys
import unittest
from pathlib import Path

from pydantic import ValidationError

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "app"))

from models import PythonAnalysisRequest, PythonAnalysisResponse  # noqa: E402


class AnalysisRequestTest(unittest.TestCase):

    def test_exercise_type_defaults_to_squat(self):
        request = PythonAnalysisRequest(sessionId=1, videoFilePath="video.mp4")
        self.assertEqual(request.exerciseType, "SQUAT")

    def test_accepts_vertical_jump_parameters(self):
        request = PythonAnalysisRequest(sessionId=1, videoFilePath="video.mp4", exerciseType="VERTICAL_JUMP",
                                        bodyWeightKg=80.0, heightCm=180.0)
        self.assertEqual(request.bodyWeightKg, 80.0)
        self.assertEqual(request.heightCm, 180.0)

    def test_rejects_unknown_exercise_type(self):
        with self.assertRaises(ValidationError):
            PythonAnalysisRequest(sessionId=1, videoFilePath="video.mp4", exerciseType="DEADLIFT")


class AnalysisResponseTest(unittest.TestCase):

    def test_squat_response_keeps_previous_shape(self):
        response = PythonAnalysisResponse(sessionId=1, totalReps=3, validReps=2, reps=[])
        body = response.model_dump()
        self.assertEqual(body["exerciseType"], "SQUAT")
        self.assertEqual(body["totalReps"], 3)
        self.assertEqual(body["validReps"], 2)

    def test_jump_response_serializes_jumps(self):
        response = PythonAnalysisResponse(
            sessionId=1, exerciseType="VERTICAL_JUMP", videoFps=30.0, lowFpsWarning=True, totalJumps=1,
            jumps=[{"jumpNumber": 1, "flightTime": 0.5, "heightFlightCm": 30.66, "takeoffVelocity": 2.45}])
        body = response.model_dump()
        self.assertEqual(body["jumps"][0]["heightFlightCm"], 30.66)
        self.assertTrue(body["lowFpsWarning"])
        self.assertIsNone(body["totalReps"])


if __name__ == "__main__":
    unittest.main()
