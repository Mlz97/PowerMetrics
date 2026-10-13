package com.BEPerfectSquat.BEPerfectSquat.domain.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.AnalysisClient;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto.PythonAnalysisResponse;

@ExtendWith(MockitoExtension.class)
class AnalysisRunnerTest {
    @Mock
    private AnalysisClient analysisClient;

    @Mock
    private AnalysisSessionService analysisSessionService;

    @InjectMocks
    private AnalysisRunner analysisRunner;

    @Test
    void storesResultsWhenAnalysisSucceeds() {
        PythonAnalysisResponse response = new PythonAnalysisResponse();
        when(analysisClient.analyzeVideo(1L, "uploads/video.mp4", 80.0)).thenReturn(response);

        analysisRunner.analyze(1L, "uploads/video.mp4", 80.0);

        verify(analysisSessionService).updateSessionWithResults(1L, response);
        verify(analysisSessionService, never()).markAsError(any());
    }

    @Test
    void marksSessionAsErrorWhenAnalysisFails() {
        when(analysisClient.analyzeVideo(1L, "uploads/video.mp4", null))
                .thenThrow(new ResourceAccessException("Python service down"));

        assertDoesNotThrow(() -> analysisRunner.analyze(1L, "uploads/video.mp4", null));

        verify(analysisSessionService).markAsError(1L);
        verify(analysisSessionService, never()).updateSessionWithResults(any(), any());
    }
}
