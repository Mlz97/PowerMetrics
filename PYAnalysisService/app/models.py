from pydantic import BaseModel
from typing import List, Literal, Optional

ExerciseType = Literal["SQUAT", "VERTICAL_JUMP"]

class PythonAnalysisRequest(BaseModel):
    sessionId: int
    videoFilePath: str
    weightKg: Optional[float] = None
    exerciseType: ExerciseType = "SQUAT"
    bodyWeightKg: Optional[float] = None
    heightCm: Optional[float] = None

class PythonRepDetail(BaseModel):
    repNumber: int
    valid: bool
    minHipAngle: Optional[float] = None
    minKneeAngle: Optional[float] = None
    concentricVelocity: Optional[float] = None

class PythonJumpDetail(BaseModel):
    jumpNumber: int
    flightTime: float
    heightFlightCm: float
    takeoffVelocity: float
    minKneeAngle: Optional[float] = None
    countermovementDepthCm: Optional[float] = None
    heightDisplacementCm: Optional[float] = None
    peakPowerW: Optional[float] = None

class PythonAnalysisResponse(BaseModel):
    sessionId: int
    exerciseType: ExerciseType = "SQUAT"
    processedVideoFilePath: Optional[str] = None
    videoFps: Optional[float] = None
    lowFpsWarning: Optional[bool] = None
    # Sentadilla
    totalReps: Optional[int] = None
    validReps: Optional[int] = None
    reps: List[PythonRepDetail] = []
    averageConcentricVelocity: Optional[float] = None
    estimatedRM: Optional[float] = None
    fatigueIndex: Optional[float] = None
    # Salto vertical
    totalJumps: Optional[int] = None
    jumps: List[PythonJumpDetail] = []
    bestJumpHeightCm: Optional[float] = None
    avgJumpHeightCm: Optional[float] = None
    peakPowerW: Optional[float] = None
