from fastapi import FastAPI
import uvicorn
from models import PythonAnalysisRequest, PythonAnalysisResponse, PythonRepDetail
from services.squat_analyzer import SquatAnalyzer

app = FastAPI(title="PerfectSquat Python Analysis Service")

@app.post("/analyze", response_model=PythonAnalysisResponse)
async def analyze_video(request: PythonAnalysisRequest):
    print(f"Received request for session: {request.sessionId}, video: {request.videoFilePath}, weight: {request.weightKg}")
    
    analyzer = SquatAnalyzer(
        session_id=request.sessionId,
        video_path=request.videoFilePath,
        weight_kg=request.weightKg
    )
    
    response = analyzer.analyze()
    
    print(f"Returning analysis: {response.validReps}/{response.totalReps} valid reps for session {request.sessionId}")
    return response

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
