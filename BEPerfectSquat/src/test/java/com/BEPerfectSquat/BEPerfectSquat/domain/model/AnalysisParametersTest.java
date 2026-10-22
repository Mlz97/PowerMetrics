package com.BEPerfectSquat.BEPerfectSquat.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.BEPerfectSquat.BEPerfectSquat.domain.exception.InvalidAnalysisParametersException;

class AnalysisParametersTest {

    @Test
    void acceptsValidJumpParameters() {
        assertDoesNotThrow(() -> new AnalysisParameters(null, 80.0, 180.0).validate());
    }

    @Test
    void acceptsMissingParameters() {
        assertDoesNotThrow(() -> AnalysisParameters.empty().validate());
    }

    @Test
    void rejectsHeightOutOfRange() {
        InvalidAnalysisParametersException ex = assertThrows(InvalidAnalysisParametersException.class,
                () -> new AnalysisParameters(null, 80.0, 90.0).validate());
        assertTrue(ex.getMessage().contains("heightCm"));
    }

    @Test
    void rejectsBodyWeightOutOfRange() {
        InvalidAnalysisParametersException ex = assertThrows(InvalidAnalysisParametersException.class,
                () -> new AnalysisParameters(null, 10.0, 180.0).validate());
        assertTrue(ex.getMessage().contains("bodyWeightKg"));
    }

    @Test
    void rejectsNegativeBarWeight() {
        InvalidAnalysisParametersException ex = assertThrows(InvalidAnalysisParametersException.class,
                () -> new AnalysisParameters(-5.0, null, null).validate());
        assertTrue(ex.getMessage().contains("weightKg"));
    }
}
