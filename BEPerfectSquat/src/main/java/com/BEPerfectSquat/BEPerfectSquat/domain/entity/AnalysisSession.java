package com.BEPerfectSquat.BEPerfectSquat.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.BEPerfectSquat.BEPerfectSquat.domain.enums.AnalysisSessionState;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.ExerciseType;

import jakarta.persistence.*;

@Entity
@Table(name = "analysis_session")
// Modelo de dominio + persistencia(JPA)
public class AnalysisSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AnalysisSessionState state = AnalysisSessionState.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type")
    private ExerciseType exerciseType = ExerciseType.SQUAT;

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
    private Video video;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepDetail> repDetails = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JumpDetail> jumpDetails = new ArrayList<>();

    private LocalDateTime createdAt;

    private Long userId;

    @Column(name = "total_reps")
    private Integer totalReps;

    @Column(name = "valid_reps")
    private Integer validReps;

    @Column(name = "estimated_rm")
    private Double estimatedRM;

    @Column(name = "fatigue_index")
    private Double fatigueIndex;

    @Column(name = "avg_concentric_velocity")
    private Double avgConcentricVelocity;

    @Column(name = "weight_kg")
    private Integer weightKg;

    @Column(name = "body_weight_kg")
    private Double bodyWeightKg;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "video_fps")
    private Double videoFps;

    @Column(name = "low_fps_warning")
    private Boolean lowFpsWarning;

    @Column(name = "total_jumps")
    private Integer totalJumps;

    @Column(name = "best_jump_height_cm")
    private Double bestJumpHeightCm;

    @Column(name = "avg_jump_height_cm")
    private Double avgJumpHeightCm;

    @Column(name = "peak_power_w")
    private Double peakPowerW;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AnalysisSessionState getState() {
        return state;
    }

    public void setState(AnalysisSessionState state) {
        this.state = state;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Double getAvgConcentricVelocity() {
        return avgConcentricVelocity;
    }

    public void setAvgConcentricVelocity(Double avgConcentricVelocity) {
        this.avgConcentricVelocity = avgConcentricVelocity;
    }

    public Integer getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Integer weightKg) {
        this.weightKg = weightKg;
    }

    public List<RepDetail> getRepDetails() {
        return repDetails;
    }

    public void setRepDetails(List<RepDetail> repDetails) {
        this.repDetails = repDetails;
    }

    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(ExerciseType exerciseType) {
        this.exerciseType = exerciseType;
    }

    public List<JumpDetail> getJumpDetails() {
        return jumpDetails;
    }

    public void setJumpDetails(List<JumpDetail> jumpDetails) {
        this.jumpDetails = jumpDetails;
    }

    public Double getBodyWeightKg() {
        return bodyWeightKg;
    }

    public void setBodyWeightKg(Double bodyWeightKg) {
        this.bodyWeightKg = bodyWeightKg;
    }

    public Double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
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
