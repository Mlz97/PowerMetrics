package com.BEPerfectSquat.BEPerfectSquat.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "jump_detail")
public class JumpDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private AnalysisSession session;

    @Column(name = "jump_number")
    private Integer jumpNumber;

    @Column(name = "flight_time")
    private Double flightTime;

    @Column(name = "height_flight_cm")
    private Double heightFlightCm;

    @Column(name = "takeoff_velocity")
    private Double takeoffVelocity;

    @Column(name = "min_knee_angle")
    private Double minKneeAngle;

    @Column(name = "countermovement_depth_cm")
    private Double countermovementDepthCm;

    @Column(name = "height_displacement_cm")
    private Double heightDisplacementCm;

    @Column(name = "peak_power_w")
    private Double peakPowerW;

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

    public Integer getJumpNumber() {
        return jumpNumber;
    }

    public void setJumpNumber(Integer jumpNumber) {
        this.jumpNumber = jumpNumber;
    }

    public Double getFlightTime() {
        return flightTime;
    }

    public void setFlightTime(Double flightTime) {
        this.flightTime = flightTime;
    }

    public Double getHeightFlightCm() {
        return heightFlightCm;
    }

    public void setHeightFlightCm(Double heightFlightCm) {
        this.heightFlightCm = heightFlightCm;
    }

    public Double getTakeoffVelocity() {
        return takeoffVelocity;
    }

    public void setTakeoffVelocity(Double takeoffVelocity) {
        this.takeoffVelocity = takeoffVelocity;
    }

    public Double getMinKneeAngle() {
        return minKneeAngle;
    }

    public void setMinKneeAngle(Double minKneeAngle) {
        this.minKneeAngle = minKneeAngle;
    }

    public Double getCountermovementDepthCm() {
        return countermovementDepthCm;
    }

    public void setCountermovementDepthCm(Double countermovementDepthCm) {
        this.countermovementDepthCm = countermovementDepthCm;
    }

    public Double getHeightDisplacementCm() {
        return heightDisplacementCm;
    }

    public void setHeightDisplacementCm(Double heightDisplacementCm) {
        this.heightDisplacementCm = heightDisplacementCm;
    }

    public Double getPeakPowerW() {
        return peakPowerW;
    }

    public void setPeakPowerW(Double peakPowerW) {
        this.peakPowerW = peakPowerW;
    }
}
