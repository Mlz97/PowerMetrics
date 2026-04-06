package com.BEPerfectSquat.BEPerfectSquat.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    public PythonAnalysisResponse analyzeVideo(Long sessionId, String videoFilePath, Double weightKg) {
        PythonAnalysisRequest request = new PythonAnalysisRequest();
        request.setSessionId(sessionId);
        request.setVideoFilePath(videoFilePath);
        request.setWeightKg(weightKg);

        ResponseEntity<PythonAnalysisResponse> response = restTemplate.postForEntity(
                pythonServiceUrl + "/analyze",
                request,
                PythonAnalysisResponse.class);

        return response.getBody();
    }

}
