package com.BEPerfectSquat.BEPerfectSquat.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.JumpDetail;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.AnalysisSessionState;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.ExerciseType;

//Modelo de salida de la API(Contrato público)
public class AnalysisSessionResponse {
    private Long id;
    private ExerciseType exerciseType;
    private AnalysisSessionState state;
    private LocalDateTime createdAt;
    private Double videoFps;
    private Boolean lowFpsWarning;

    // Sentadilla
    private Integer totalReps;
    private Integer validReps;
    private Double avgConcentricVelocity;
    private Double estimatedRM;
    private Integer weightKg;

    // Salto vertical
    private Double bodyWeightKg;
    private Double heightCm;
    private Integer totalJumps;
    private Double bestJumpHeightCm;
    private Double avgJumpHeightCm;
    private Double peakPowerW;
    private List<JumpResponse> jumps;

    private Double fatigueIndex;

    public record JumpResponse(Integer jumpNumber, Double flightTime, Double heightFlightCm, Double takeoffVelocity,
                               Double minKneeAngle, Double countermovementDepthCm, Double heightDisplacementCm,
                               Double peakPowerW) {

        static JumpResponse from(JumpDetail jump) {
            return new JumpResponse(jump.getJumpNumber(), jump.getFlightTime(), jump.getHeightFlightCm(),
                    jump.getTakeoffVelocity(), jump.getMinKneeAngle(), jump.getCountermovementDepthCm(),
                    jump.getHeightDisplacementCm(), jump.getPeakPowerW());
        }
    }

    public static AnalysisSessionResponse from(AnalysisSession analysisSession){
        AnalysisSessionResponse response = new AnalysisSessionResponse();
        response.id = analysisSession.getId();
        response.exerciseType = analysisSession.getExerciseType();
        response.state = analysisSession.getState();
        response.createdAt = analysisSession.getCreatedAt();
        response.videoFps = analysisSession.getVideoFps();
        response.lowFpsWarning = analysisSession.getLowFpsWarning();
        response.totalReps = analysisSession.getTotalReps();
        response.validReps = analysisSession.getValidReps();
        response.avgConcentricVelocity = analysisSession.getAvgConcentricVelocity();
        response.estimatedRM = analysisSession.getEstimatedRM();
        response.weightKg = analysisSession.getWeightKg();
        response.bodyWeightKg = analysisSession.getBodyWeightKg();
        response.heightCm = analysisSession.getHeightCm();
        response.totalJumps = analysisSession.getTotalJumps();
        response.bestJumpHeightCm = analysisSession.getBestJumpHeightCm();
        response.avgJumpHeightCm = analysisSession.getAvgJumpHeightCm();
        response.peakPowerW = analysisSession.getPeakPowerW();
        response.jumps = analysisSession.getJumpDetails().stream().map(JumpResponse::from).toList();
        response.fatigueIndex = analysisSession.getFatigueIndex();
        return response;
    }

    public Long getId() {
        return id;
    }

    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    public AnalysisSessionState getState() {
        return state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Double getVideoFps() {
        return videoFps;
    }

    public Boolean getLowFpsWarning() {
        return lowFpsWarning;
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

    public Integer getWeightKg() {
        return weightKg;
    }

    public Double getBodyWeightKg() {
        return bodyWeightKg;
    }

    public Double getHeightCm() {
        return heightCm;
    }

    public Integer getTotalJumps() {
        return totalJumps;
    }

    public Double getBestJumpHeightCm() {
        return bestJumpHeightCm;
    }

    public Double getAvgJumpHeightCm() {
        return avgJumpHeightCm;
    }

    public Double getPeakPowerW() {
        return peakPowerW;
    }

    public List<JumpResponse> getJumps() {
        return jumps;
    }

    public Double getFatigueIndex() {
        return fatigueIndex;
    }
}
