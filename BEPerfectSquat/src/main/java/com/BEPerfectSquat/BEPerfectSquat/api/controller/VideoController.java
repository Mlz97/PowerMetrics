package com.BEPerfectSquat.BEPerfectSquat.api.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.BEPerfectSquat.BEPerfectSquat.api.dto.response.VideoResponse;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.Video;
import com.BEPerfectSquat.BEPerfectSquat.domain.service.VideoService;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.service.StorageService;

@RestController
@RequestMapping("/video")
public class VideoController {
    private final VideoService videoService;
    private final StorageService storageService;

    public VideoController(VideoService videoService, StorageService storageService) {
        this.videoService = videoService;
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<VideoResponse> getVideos(@RequestParam Long sessionId) {
        Video video = videoService.getVideoBySessionId(sessionId);
        VideoResponse response = VideoResponse.from(video);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{sessionId}")
    public ResponseEntity<Resource> downloadProcessedVideo(@PathVariable Long sessionId) {
        Video video = videoService.getVideoBySessionId(sessionId);
        
        String processedPath = video.getProcessedFilePath();
        if (processedPath == null || processedPath.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = storageService.loadAsResource(processedPath);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"processed_video.mp4\"")
                .body(resource);
    }

}
