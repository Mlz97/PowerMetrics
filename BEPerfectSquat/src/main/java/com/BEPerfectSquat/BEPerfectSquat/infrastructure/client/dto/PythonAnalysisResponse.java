package com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto;

import java.util.List;

public class PythonAnalysisResponse {
    private Long sessionId;
    private String processedVideoFilePath;
    private Integer totalReps;
    private Integer validReps;
    private List<PythonRepDetail> reps;
    private Double averageConcentricVelocity;
    private Double estimatedRM;
    private Double fatigueIndex;
    private String exerciseType;
    private Double videoFps;
    private Boolean lowFpsWarning;
    private Integer totalJumps;
    private List<PythonJumpDetail> jumps;
    private Double bestJumpHeightCm;
    private Double avgJumpHeightCm;
    private Double peakPowerW;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getProcessedVideoFilePath() {
        return processedVideoFilePath;
    }

    public void setProcessedVideoFilePath(String processedVideoFilePath) {
        this.processedVideoFilePath = processedVideoFilePath;
    }

    public Integer getTotalReps() {
        return totalReps;
    }

    public void setTotalReps(Integer totalReps) {
        this.totalReps = totalReps;
    }

    public Integer getValidReps() {
        return validReps;
    }

    public void setValidReps(Integer validReps) {
        this.validReps = validReps;
    }

    public List<PythonRepDetail> getReps() {
        return reps;
    }

    public void setReps(List<PythonRepDetail> reps) {
        this.reps = reps;
    }

    public Double getAverageConcentricVelocity() {
        return averageConcentricVelocity;
    }

    public void setAverageConcentricVelocity(Double averageConcentricVelocity) {
        this.averageConcentricVelocity = averageConcentricVelocity;
    }

    public Double getEstimatedRM() {
        return estimatedRM;
    }

    public void setEstimatedRM(Double estimatedRM) {
        this.estimatedRM = estimatedRM;
    }

    public Double getFatigueIndex() {
        return fatigueIndex;
    }

    public void setFatigueIndex(Double fatigueIndex) {
        this.fatigueIndex = fatigueIndex;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public Double getVideoFps() {
        return videoFps;
    }

    public void setVideoFps(Double videoFps) {
        this.videoFps = videoFps;
    }

    public Boolean getLowFpsWarning() {
        return lowFpsWarning;
    }

    public void setLowFpsWarning(Boolean lowFpsWarning) {
        this.lowFpsWarning = lowFpsWarning;
    }

    public Integer getTotalJumps() {
        return totalJumps;
    }

    public void setTotalJumps(Integer totalJumps) {
        this.totalJumps = totalJumps;
    }

    public List<PythonJumpDetail> getJumps() {
        return jumps;
    }

    public void setJumps(List<PythonJumpDetail> jumps) {
        this.jumps = jumps;
    }

    public Double getBestJumpHeightCm() {
        return bestJumpHeightCm;
    }

    public void setBestJumpHeightCm(Double bestJumpHeightCm) {
        this.bestJumpHeightCm = bestJumpHeightCm;
    }

    public Double getAvgJumpHeightCm() {
        return avgJumpHeightCm;
    }

    public void setAvgJumpHeightCm(Double avgJumpHeightCm) {
        this.avgJumpHeightCm = avgJumpHeightCm;
    }

    public Double getPeakPowerW() {
        return peakPowerW;
    }

    public void setPeakPowerW(Double peakPowerW) {
        this.peakPowerW = peakPowerW;
    }
}
