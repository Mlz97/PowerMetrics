from fastapi import FastAPI, HTTPException
import uvicorn
import logging
from models import PythonAnalysisRequest, PythonAnalysisResponse, PythonRepDetail
from services.squat_analyzer import SquatAnalyzer
from services.jump_analyzer import JumpAnalyzer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="PerfectSquat Python Analysis Service")

@app.post("/analyze", response_model=PythonAnalysisResponse)
def analyze_video(request: PythonAnalysisRequest):
    logger.info(f"Received {request.exerciseType} request for session: {request.sessionId}, video: {request.videoFilePath}, weight: {request.weightKg}")
    
    try:
        if request.exerciseType == "VERTICAL_JUMP":
            analyzer = JumpAnalyzer(
                session_id=request.sessionId,
                video_path=request.videoFilePath,
                body_weight_kg=request.bodyWeightKg,
                height_cm=request.heightCm
            )
            response = analyzer.analyze()
            logger.info(f"Returning analysis: {response.totalJumps} jumps for session {request.sessionId}")
        else:
            analyzer = SquatAnalyzer(
                session_id=request.sessionId,
                video_path=request.videoFilePath,
                weight_kg=request.weightKg
            )
            response = analyzer.analyze()
            logger.info(f"Returning analysis: {response.validReps}/{response.totalReps} valid reps for session {request.sessionId}")
        return response
    except Exception as e:
        logger.exception(f"Error analyzing video {request.videoFilePath} for session {request.sessionId}: {e}")
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
