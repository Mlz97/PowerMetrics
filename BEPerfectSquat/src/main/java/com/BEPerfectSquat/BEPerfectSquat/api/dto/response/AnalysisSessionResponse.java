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

    public AnalysisSessionResponse(Long id, AnalysisSessionState state, LocalDateTime createdAt, Integer totalReps, Integer validReps){
        this.id=id;
        this.state=state;
        this.createdAt=createdAt;
        this.totalReps = totalReps;
        this.validReps = validReps;
    }

    public static AnalysisSessionResponse from(AnalysisSession analysisSession){
        return new AnalysisSessionResponse(
            analysisSession.getId(),
            analysisSession.getState(),
            analysisSession.getCreatedAt(),
            analysisSession.getTotalReps(),
            analysisSession.getValidReps()
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
}
