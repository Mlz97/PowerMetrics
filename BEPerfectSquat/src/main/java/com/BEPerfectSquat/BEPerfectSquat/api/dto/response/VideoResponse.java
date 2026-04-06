package com.BEPerfectSquat.BEPerfectSquat.api.dto.response;

import java.time.LocalDateTime;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.Video;

public class VideoResponse {
    private Long id;
    private Long sessionId;
    private String filePath;
    private LocalDateTime uploadedAt;
    private String processedFilePath;

    public VideoResponse(Long id, Long sessionId, String filePath, LocalDateTime uploadedAt, String processedFilePath) {
        this.id = id;
        this.sessionId = sessionId;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
        this.processedFilePath = processedFilePath;
    }

    public static VideoResponse from(Video video) {
        return new VideoResponse(
                video.getId(),
                video.getSession().getId(),
                video.getFilePath(),
                video.getUploadedAt(),
                video.getProcessedFilePath());
    }

    public Long getId() {
        return id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getFilePath() {
        return filePath;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public String getProcessedFilePath() {
        return processedFilePath;
    }

}
