package com.BEPerfectSquat.BEPerfectSquat.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.BEPerfectSquat.BEPerfectSquat.api.dto.response.VideoResponse;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.Video;
import com.BEPerfectSquat.BEPerfectSquat.domain.service.VideoService;

@RestController
@RequestMapping("/video")
public class VideoController {
    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoResponse> createVideo(
            @RequestParam("sessionId") Long sessionId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "weightKg", required = false) Double weightKg) {
        
        Video video = videoService.saveVideo(sessionId, file, weightKg);
        VideoResponse response = VideoResponse.from(video);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<VideoResponse> getVideos(@RequestParam Long sessionId) {
        Video video = videoService.getVideoBySessionId(sessionId);
        VideoResponse response = VideoResponse.from(video);
        return ResponseEntity.ok(response);
    }

}
