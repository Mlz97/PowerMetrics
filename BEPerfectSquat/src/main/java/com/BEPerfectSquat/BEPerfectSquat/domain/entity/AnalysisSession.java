package com.BEPerfectSquat.BEPerfectSquat.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.BEPerfectSquat.BEPerfectSquat.domain.enums.AnalysisSessionState;

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

    @OneToOne(mappedBy = "session")
    private Video video;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepDetail> repDetails = new ArrayList<>();

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

}
