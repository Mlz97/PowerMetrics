package com.BEPerfectSquat.BEPerfectSquat.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.BEPerfectSquat.BEPerfectSquat.api.dto.response.AnalysisSessionResponse;
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
    public ResponseEntity<AnalysisSessionResponse> createSession(){
        AnalysisSession session = analysisSessionService.createSession();
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
            @RequestParam(value = "weightKg", required = false) Double weightKg) {
        
        videoService.saveVideo(id, file, weightKg);
        AnalysisSession session = analysisSessionService.getSessionById(id);
        return ResponseEntity.ok(AnalysisSessionResponse.from(session));
    }


}
