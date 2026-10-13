package com.BEPerfectSquat.BEPerfectSquat.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.BEPerfectSquat.BEPerfectSquat.api.dto.request.CreateSessionRequest;
import com.BEPerfectSquat.BEPerfectSquat.api.dto.response.AnalysisSessionResponse;
import com.BEPerfectSquat.BEPerfectSquat.domain.model.AnalysisParameters;
import org.springframework.web.bind.annotation.RequestBody;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.service.AnalysisSessionService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.BEPerfectSquat.BEPerfectSquat.domain.service.VideoService;



//Adaptador HTTP
@RestController
@RequestMapping("/session")
public class AnalysisSessionController {
    private final AnalysisSessionService analysisSessionService;
    private final VideoService videoService;

    public AnalysisSessionController(AnalysisSessionService analysisSessionService, VideoService videoService){
        this.analysisSessionService=analysisSessionService;
        this.videoService = videoService;
    }

    @PostMapping
    public ResponseEntity<AnalysisSessionResponse> createSession(
            @RequestBody(required = false) CreateSessionRequest request){
        // Sin body se crea una sesión de sentadilla, como antes de añadir el salto vertical
        CreateSessionRequest sessionRequest = request != null ? request : new CreateSessionRequest();
        AnalysisSession session = analysisSessionService.createSession(sessionRequest.getExerciseType());
        AnalysisSessionResponse response = AnalysisSessionResponse.from(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisSessionResponse> getAnalysisSession(@PathVariable Long id) {
        AnalysisSession session  = analysisSessionService.getSessionById(id);
        AnalysisSessionResponse response = AnalysisSessionResponse.from(session);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/video")
    public ResponseEntity<AnalysisSessionResponse> uploadVideo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "weightKg", required = false) Double weightKg,
            @RequestParam(value = "bodyWeightKg", required = false) Double bodyWeightKg,
            @RequestParam(value = "heightCm", required = false) Double heightCm) {

        videoService.saveVideo(id, file, new AnalysisParameters(weightKg, bodyWeightKg, heightCm));
        AnalysisSession session = analysisSessionService.getSessionById(id);
        // 202: el análisis sigue en curso, consultar GET /session/{id} hasta FINISHED o ERROR
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(AnalysisSessionResponse.from(session));
    }


}
