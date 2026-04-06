package com.BEPerfectSquat.BEPerfectSquat.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rep_detail")
public class RepDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private AnalysisSession session;

    @Column(name = "rep_number")
    private Integer repNumber;

    @Column(name = "valid")
    private Boolean valid;

    @Column(name = "min_hip_angle")
    private Double minHipAngle;

    @Column(name = "min_knee_angle")
    private Double minKneeAngle;

    @Column(name = "concentric_velocity")
    private Double concentricVelocity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AnalysisSession getSession() {
        return session;
    }

    public void setSession(AnalysisSession session) {
        this.session = session;
    }

    public Integer getRepNumber() {
        return repNumber;
    }

    public void setRepNumber(Integer repNumber) {
        this.repNumber = repNumber;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Double getMinHipAngle() {
        return minHipAngle;
    }

    public void setMinHipAngle(Double minHipAngle) {
        this.minHipAngle = minHipAngle;
    }

    public Double getMinKneeAngle() {
        return minKneeAngle;
    }

    public void setMinKneeAngle(Double minKneeAngle) {
        this.minKneeAngle = minKneeAngle;
    }

    public Double getConcentricVelocity() {
        return concentricVelocity;
    }

    public void setConcentricVelocity(Double concentricVelocity) {
        this.concentricVelocity = concentricVelocity;
    }
}
