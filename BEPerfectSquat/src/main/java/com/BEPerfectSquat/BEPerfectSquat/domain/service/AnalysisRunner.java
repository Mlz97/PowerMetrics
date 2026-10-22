package com.BEPerfectSquat.BEPerfectSquat.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.BEPerfectSquat.BEPerfectSquat.domain.enums.ExerciseType;
import com.BEPerfectSquat.BEPerfectSquat.domain.model.AnalysisParameters;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.AnalysisClient;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto.PythonAnalysisResponse;

// Ejecuta el análisis en segundo plano para no bloquear la petición HTTP
@Service
public class AnalysisRunner {
    private static final Logger log = LoggerFactory.getLogger(AnalysisRunner.class);

    private final AnalysisClient analysisClient;
    private final AnalysisSessionService analysisSessionService;

    public AnalysisRunner(AnalysisClient analysisClient, AnalysisSessionService analysisSessionService) {
        this.analysisClient = analysisClient;
        this.analysisSessionService = analysisSessionService;
    }

    @Async
    public void analyze(Long sessionId, String videoFilePath, ExerciseType exerciseType,
                        AnalysisParameters parameters) {
        try {
            PythonAnalysisResponse response = analysisClient.analyzeVideo(sessionId, videoFilePath, exerciseType, parameters);
            analysisSessionService.updateSessionWithResults(sessionId, response);
        } catch (Exception e) {
            log.error("Analysis failed for session {}", sessionId, e);
            analysisSessionService.markAsError(sessionId);
        }
    }
}
