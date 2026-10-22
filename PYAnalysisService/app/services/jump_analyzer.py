import math

import cv2
import mediapipe as mp

from models import PythonAnalysisResponse, PythonJumpDetail
from services.jump_metrics import FrameSample, analyze_jumps, is_low_fps, resolve_fps

MIN_LEG_VISIBILITY = 0.3
MIN_FOOT_VISIBILITY = 0.5


class JumpAnalyzer:
    def __init__(self, session_id: int, video_path: str, body_weight_kg: float = None, height_cm: float = None):
        self.session_id = session_id
        self.video_path = video_path
        self.body_weight_kg = body_weight_kg
        self.height_cm = height_cm

        self.mp_drawing = mp.solutions.drawing_utils
        self.mp_pose = mp.solutions.pose

    @staticmethod
    def calculate_angle(a, b, c):
        """Angulo en el vertice b formado por los puntos a b c."""
        radians = math.atan2(c[1] - b[1], c[0] - b[0]) - math.atan2(a[1] - b[1], a[0] - b[0])
        angle = abs(math.degrees(radians))
        return 360 - angle if angle > 180.0 else angle

    def _point(self, landmarks, landmark, width, height):
        lm = landmarks[landmark.value]
        return (lm.x * width, lm.y * height), lm.visibility

    def _sample(self, landmarks, width, height) -> FrameSample:
        pose = self.mp_pose.PoseLandmark
        sides = {
            "LEFT": (pose.LEFT_HIP, pose.LEFT_KNEE, pose.LEFT_ANKLE),
            "RIGHT": (pose.RIGHT_HIP, pose.RIGHT_KNEE, pose.RIGHT_ANKLE),
        }
        # Lado mas visible del cuerpo (el video se graba de perfil)
        best_side, best_visibility = None, 0.0
        for side, joints in sides.items():
            visibility = sum(landmarks[j.value].visibility for j in joints) / 3.0
            if visibility > best_visibility:
                best_side, best_visibility = side, visibility

        hip_y = knee_angle = leg_length = None
        if best_visibility >= MIN_LEG_VISIBILITY:
            hip, knee, ankle = (self._point(landmarks, j, width, height)[0] for j in sides[best_side])
            hip_y = hip[1]
            knee_angle = self.calculate_angle(hip, knee, ankle)
            leg_length = math.dist(hip, ankle)

        # Altura de los pies: media de talones y puntas visibles de ambos pies
        feet = [pose.LEFT_HEEL, pose.RIGHT_HEEL, pose.LEFT_FOOT_INDEX, pose.RIGHT_FOOT_INDEX]
        foot_points = [self._point(landmarks, f, width, height) for f in feet]
        visible_feet = [p[1] for p, visibility in foot_points if visibility >= MIN_FOOT_VISIBILITY]
        foot_y = sum(visible_feet) / len(visible_feet) if visible_feet else None

        return FrameSample(foot_y=foot_y, hip_y=hip_y, knee_angle=knee_angle, leg_length_px=leg_length)

    def analyze(self) -> PythonAnalysisResponse:
        cap = cv2.VideoCapture(self.video_path)
        if not cap.isOpened():
            raise RuntimeError(f"OpenCV no pudo abrir el archivo de video: {self.video_path}")

        fps = resolve_fps(cap.get(cv2.CAP_PROP_FPS))
        width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

        out_path = self.video_path.replace(".mp4", "_analyzed.mp4")
        out = cv2.VideoWriter(out_path, cv2.VideoWriter_fourcc(*'mp4v'), fps, (width, height))

        frames = []
        with self.mp_pose.Pose(min_detection_confidence=0.5, min_tracking_confidence=0.5) as pose:
            while cap.isOpened():
                ret, frame = cap.read()
                if not ret:
                    break

                image = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                image.flags.writeable = False
                results = pose.process(image)
                image.flags.writeable = True
                image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)

                if results.pose_landmarks:
                    sample = self._sample(results.pose_landmarks.landmark, width, height)
                    self.mp_drawing.draw_landmarks(image, results.pose_landmarks, self.mp_pose.POSE_CONNECTIONS)
                    if sample.knee_angle is not None:
                        cv2.putText(image, f"KNEE: {int(sample.knee_angle)}", (15, 40),
                                    cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2, cv2.LINE_AA)
                else:
                    sample = FrameSample(foot_y=None, hip_y=None, knee_angle=None, leg_length_px=None)

                frames.append(sample)
                out.write(image)

        cap.release()
        out.release()

        metrics = analyze_jumps(frames, fps, self.body_weight_kg, self.height_cm)

        return PythonAnalysisResponse(
            sessionId=self.session_id,
            exerciseType="VERTICAL_JUMP",
            processedVideoFilePath=out_path,
            videoFps=fps,
            lowFpsWarning=is_low_fps(fps),
            totalJumps=len(metrics.jumps),
            jumps=[PythonJumpDetail(**vars(jump)) for jump in metrics.jumps],
            bestJumpHeightCm=metrics.bestJumpHeightCm,
            avgJumpHeightCm=metrics.avgJumpHeightCm,
            peakPowerW=metrics.peakPowerW,
            fatigueIndex=metrics.fatigueIndex,
        )
