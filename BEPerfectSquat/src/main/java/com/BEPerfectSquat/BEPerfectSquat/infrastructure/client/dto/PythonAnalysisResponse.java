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

}
