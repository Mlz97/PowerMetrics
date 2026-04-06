from pydantic import BaseModel
from typing import List, Optional

class PythonAnalysisRequest(BaseModel):
    sessionId: int
    videoFilePath: str
    weightKg: Optional[float] = None

class PythonRepDetail(BaseModel):
    repNumber: int
    valid: bool
    minHipAngle: Optional[float] = None
    minKneeAngle: Optional[float] = None
    concentricVelocity: Optional[float] = None

class PythonAnalysisResponse(BaseModel):
    sessionId: int
    processedVideoFilePath: Optional[str] = None
    totalReps: int
    validReps: int
    reps: List[PythonRepDetail]
    averageConcentricVelocity: Optional[float] = None
    estimatedRM: Optional[float] = None
    fatigueIndex: Optional[float] = None
