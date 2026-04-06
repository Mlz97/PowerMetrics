package com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto;

public class PythonRepDetail {
    private Integer repNumber;
    private Boolean valid;
    private Double minHipAngle;
    private Double minKneeAngle;
    private Double concentricVelocity;

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
