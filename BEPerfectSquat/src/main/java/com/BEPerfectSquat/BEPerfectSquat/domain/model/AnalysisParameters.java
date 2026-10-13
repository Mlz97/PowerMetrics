package com.BEPerfectSquat.BEPerfectSquat.domain.model;

import com.BEPerfectSquat.BEPerfectSquat.domain.exception.InvalidAnalysisParametersException;

// Parámetros del análisis que introduce el usuario; todos son opcionales
public record AnalysisParameters(Double weightKg, Double bodyWeightKg, Double heightCm) {
    private static final double MAX_BAR_WEIGHT_KG = 500;
    private static final double MIN_BODY_WEIGHT_KG = 20;
    private static final double MAX_BODY_WEIGHT_KG = 300;
    private static final double MIN_HEIGHT_CM = 100;
    private static final double MAX_HEIGHT_CM = 250;

    public static AnalysisParameters empty() {
        return new AnalysisParameters(null, null, null);
    }

    public void validate() {
        checkRange("weightKg", weightKg, 0, MAX_BAR_WEIGHT_KG);
        checkRange("bodyWeightKg", bodyWeightKg, MIN_BODY_WEIGHT_KG, MAX_BODY_WEIGHT_KG);
        checkRange("heightCm", heightCm, MIN_HEIGHT_CM, MAX_HEIGHT_CM);
    }

    private static void checkRange(String name, Double value, double min, double max) {
        if (value != null && (value < min || value > max)) {
            throw new InvalidAnalysisParametersException(
                    String.format("%s debe estar entre %.0f y %.0f", name, min, max));
        }
    }
}
