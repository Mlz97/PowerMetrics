from fastapi import FastAPI, HTTPException
import uvicorn
import logging
from models import PythonAnalysisRequest, PythonAnalysisResponse, PythonRepDetail
from services.squat_analyzer import SquatAnalyzer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="PerfectSquat Python Analysis Service")

@app.post("/analyze", response_model=PythonAnalysisResponse)
def analyze_video(request: PythonAnalysisRequest):
    logger.info(f"Received request for session: {request.sessionId}, video: {request.videoFilePath}, weight: {request.weightKg}")
    
    try:
        analyzer = SquatAnalyzer(
            session_id=request.sessionId,
            video_path=request.videoFilePath,
            weight_kg=request.weightKg
        )
        
        response = analyzer.analyze()
        
        logger.info(f"Returning analysis: {response.validReps}/{response.totalReps} valid reps for session {request.sessionId}")
        return response
    except Exception as e:
        import traceback
        with open("error_log.txt", "a") as f:
            f.write(f"Exception for session {request.sessionId}:\n")
            f.write(traceback.format_exc())
            f.write("\n")
        logger.error(f"Error analyzing video {request.videoFilePath}: {e}")
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
