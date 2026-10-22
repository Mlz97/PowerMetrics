package com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto;

public class PythonJumpDetail {
    private Integer jumpNumber;

    private Double flightTime;

    private Double heightFlightCm;

    private Double takeoffVelocity;

    private Double minKneeAngle;

    private Double countermovementDepthCm;

    private Double heightDisplacementCm;

    private Double peakPowerW;

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
