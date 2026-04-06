import cv2
import mediapipe as mp
import numpy as np
import math
from models import PythonRepDetail, PythonAnalysisResponse

class SquatAnalyzer:
    def __init__(self, session_id: int, video_path: str, weight_kg: float = None):
        self.session_id = session_id
        self.video_path = video_path
        self.weight_kg = weight_kg
        
        self.mp_drawing = mp.solutions.drawing_utils
        self.mp_pose = mp.solutions.pose
        
        # Variables estado
        self.state = "STANDING" # STANDING, DESCENDING, ASCENDING
        self.rep_count = 0
        self.valid_reps = 0
        self.reps_details = []
        
        # track reps
        self.current_min_knee_angle = 180.0
        self.current_min_hip_angle = 180.0
        self.is_current_rep_valid = False
        
        # prueba track velocidad (pixel/segundo)
        self.fps = 30
        self.frames_in_ascent = 0

    def calculate_angle(self, a, b, c):
        """Calculo de angulo entre 3 puntos: a b c."""
        a = np.array(a) # Comienzo
        b = np.array(b) # Vertice
        c = np.array(c) # Final
        
        radians = np.arctan2(c[1]-b[1], c[0]-b[0]) - np.arctan2(a[1]-b[1], a[0]-b[0])
        angle = np.abs(radians*180.0/np.pi)
        
        if angle > 180.0:
            angle = 360-angle
            
        return angle

    def analyze(self) -> PythonAnalysisResponse:
        cap = cv2.VideoCapture(self.video_path)
        
        if not cap.isOpened():
            print(f"Error abriendo archivo de video {self.video_path}")
            return PythonAnalysisResponse(
                sessionId=self.session_id,
                totalReps=0,
                validReps=0,
                reps=[]
            )

        self.fps = cap.get(cv2.CAP_PROP_FPS)
        if self.fps == 0 or math.isnan(self.fps):
            self.fps = 30.0

        width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
        
        # Preparar grabacion de archivo de salida
        out_path = self.video_path.replace(".mp4", "_analyzed.mp4")
        fourcc = cv2.VideoWriter_fourcc(*'mp4v')
        out = cv2.VideoWriter(out_path, fourcc, self.fps, (width, height))

        total_velocity = 0.0

        with self.mp_pose.Pose(min_detection_confidence=0.5, min_tracking_confidence=0.5) as pose:
            while cap.isOpened():
                ret, frame = cap.read()
                if not ret:
                    break
                
                # Recolor  RGB
                image = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                image.flags.writeable = False
                
                # deteccion
                results = pose.process(image)
                
                # Recolor BGR
                image.flags.writeable = True
                image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)
                
                # landmarks
                try:
                    landmarks = results.pose_landmarks.landmark
                    
                    # lado izquierdo por defecto
                    hip = [landmarks[self.mp_pose.PoseLandmark.LEFT_HIP.value].x, landmarks[self.mp_pose.PoseLandmark.LEFT_HIP.value].y]
                    knee = [landmarks[self.mp_pose.PoseLandmark.LEFT_KNEE.value].x, landmarks[self.mp_pose.PoseLandmark.LEFT_KNEE.value].y]
                    ankle = [landmarks[self.mp_pose.PoseLandmark.LEFT_ANKLE.value].x, landmarks[self.mp_pose.PoseLandmark.LEFT_ANKLE.value].y]
                    shoulder = [landmarks[self.mp_pose.PoseLandmark.LEFT_SHOULDER.value].x, landmarks[self.mp_pose.PoseLandmark.LEFT_SHOULDER.value].y]
                    
                    # Calculo angulo
                    knee_angle = self.calculate_angle(hip, knee, ankle)
                    hip_angle = self.calculate_angle(shoulder, hip, knee)
                    
                    # Visualise angle
                    cv2.putText(image, str(int(knee_angle)),
                                tuple(np.multiply(knee, [width, height]).astype(int)),
                                cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2, cv2.LINE_AA)
                    
                    # track estado
                    if knee_angle > 160:
                        if self.state == "ASCENDING":
                            # termina rep
                            self.rep_count += 1
                            
                            # Validacion profundidad
                            if self.current_min_knee_angle <= 90.0:
                                self.is_current_rep_valid = True
                                self.valid_reps += 1
                                
                            # Aproximacion velocidad
                            # min -> parado en frames_in_ascent
                            # Tiempo = frames / fps
                            ascent_time = self.frames_in_ascent / self.fps if self.frames_in_ascent > 0 else 1.0
                            calc_velocity = 1.0 / ascent_time
                            total_velocity += calc_velocity

                            self.reps_details.append(PythonRepDetail(
                                repNumber=self.rep_count,
                                valid=self.is_current_rep_valid,
                                minHipAngle=self.current_min_hip_angle,
                                minKneeAngle=self.current_min_knee_angle,
                                concentricVelocity=calc_velocity
                            ))
                            
                            # Reset estado
                            self.current_min_knee_angle = 180.0
                            self.current_min_hip_angle = 180.0
                            self.is_current_rep_valid = False
                            self.frames_in_ascent = 0
                            
                        self.state = "STANDING"
                        
                    if knee_angle < 150:
                        if self.state == "STANDING":
                            self.state = "DESCENDING"
                            
                        if self.state == "DESCENDING":
                            # Track minimos
                            if knee_angle < self.current_min_knee_angle:
                                self.current_min_knee_angle = knee_angle
                            if hip_angle < self.current_min_hip_angle:
                                self.current_min_hip_angle = hip_angle
                                
                            # Si de repente empieza a subir el angulo crece +10 del minimo
                            if knee_angle > self.current_min_knee_angle + 10:
                                self.state = "ASCENDING"
                                
                        if self.state == "ASCENDING":
                            self.frames_in_ascent += 1
                            
                    # Estado render
                    cv2.putText(image, f"STATE: {self.state}", (15, 40), 
                                cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 0), 2, cv2.LINE_AA)
                    cv2.putText(image, f"REPS: {self.valid_reps}/{self.rep_count}", (15, 80), 
                                cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255) if self.state == "STANDING" else (0, 255, 0), 2, cv2.LINE_AA)

                except Exception as e:
                    pass
                
                # Render detections
                self.mp_drawing.draw_landmarks(image, results.pose_landmarks, self.mp_pose.POSE_CONNECTIONS)
                
                out.write(image)

        cap.release()
        out.release()
        
        avg_velocity = (total_velocity / self.valid_reps) if self.valid_reps > 0 else 0.0
        
        # Epley 1RM = Weight * (1 + 0.0333 * Reps)
        est_rm = self.weight_kg * (1 + 0.0333 * self.valid_reps) if self.weight_kg else 0.0
        
        return PythonAnalysisResponse(
            sessionId=self.session_id,
            processedVideoFilePath=out_path,
            totalReps=self.rep_count,
            validReps=self.valid_reps,
            reps=self.reps_details,
            averageConcentricVelocity=avg_velocity,
            estimatedRM=est_rm,
            fatigueIndex=0.0
        )
