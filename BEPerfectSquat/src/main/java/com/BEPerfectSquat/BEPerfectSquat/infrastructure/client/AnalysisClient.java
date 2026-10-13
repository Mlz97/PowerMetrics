package com.BEPerfectSquat.BEPerfectSquat.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.BEPerfectSquat.BEPerfectSquat.domain.enums.ExerciseType;
import com.BEPerfectSquat.BEPerfectSquat.domain.model.AnalysisParameters;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto.PythonAnalysisRequest;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto.PythonAnalysisResponse;

@Component
public class AnalysisClient {

    private final RestTemplate restTemplate;
    private final String pythonServiceUrl;

    public AnalysisClient(
            RestTemplate restTemplate,
            @Value("${python.service.url}") String pythonServiceUrl) {
        this.restTemplate = restTemplate;
        this.pythonServiceUrl = pythonServiceUrl;
    }

    public PythonAnalysisResponse analyzeVideo(Long sessionId, String videoFilePath, ExerciseType exerciseType,
                                               AnalysisParameters parameters) {
        PythonAnalysisRequest request = new PythonAnalysisRequest();
        request.setSessionId(sessionId);
        request.setVideoFilePath(videoFilePath);
        request.setExerciseType(exerciseType.name());
        request.setWeightKg(parameters.weightKg());
        request.setBodyWeightKg(parameters.bodyWeightKg());
        request.setHeightCm(parameters.heightCm());

        ResponseEntity<PythonAnalysisResponse> response = restTemplate.postForEntity(
                pythonServiceUrl + "/analyze",
                request,
                PythonAnalysisResponse.class);

        return response.getBody();
    }

}
