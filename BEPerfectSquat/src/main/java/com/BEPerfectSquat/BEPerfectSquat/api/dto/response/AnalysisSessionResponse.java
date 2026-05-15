package com.BEPerfectSquat.BEPerfectSquat.api.dto.response;

import java.time.LocalDateTime;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.AnalysisSessionState;

//Modelo de salida de la API(Contrato público)
public class AnalysisSessionResponse {
    private Long id;
    private AnalysisSessionState state;
    private LocalDateTime createdAt;
    private Integer totalReps;
    private Integer validReps;
    private Double avgConcentricVelocity;
    private Double estimatedRM;
    private Double fatigueIndex;
    private Integer weightKg;

    public AnalysisSessionResponse(Long id, AnalysisSessionState state, LocalDateTime createdAt,
                                   Integer totalReps, Integer validReps,
                                   Double avgConcentricVelocity, Double estimatedRM,
                                   Double fatigueIndex, Integer weightKg){
        this.id=id;
        this.state=state;
        this.createdAt=createdAt;
        this.totalReps = totalReps;
        this.validReps = validReps;
        this.avgConcentricVelocity = avgConcentricVelocity;
        this.estimatedRM = estimatedRM;
        this.fatigueIndex = fatigueIndex;
        this.weightKg = weightKg;
    }

    public static AnalysisSessionResponse from(AnalysisSession analysisSession){
        return new AnalysisSessionResponse(
            analysisSession.getId(),
            analysisSession.getState(),
            analysisSession.getCreatedAt(),
            analysisSession.getTotalReps(),
            analysisSession.getValidReps(),
            analysisSession.getAvgConcentricVelocity(),
            analysisSession.getEstimatedRM(),
            analysisSession.getFatigueIndex(),
            analysisSession.getWeightKg()
        );
    }

    public Long getId() {
        return id;
    }

    public AnalysisSessionState getState() {
        return state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getTotalReps() {
        return totalReps;
    }

    public Integer getValidReps() {
        return validReps;
    }

    public Double getAvgConcentricVelocity() {
        return avgConcentricVelocity;
    }

    public Double getEstimatedRM() {
        return estimatedRM;
    }

    public Double getFatigueIndex() {
        return fatigueIndex;
    }

    public Integer getWeightKg() {
        return weightKg;
    }
}
