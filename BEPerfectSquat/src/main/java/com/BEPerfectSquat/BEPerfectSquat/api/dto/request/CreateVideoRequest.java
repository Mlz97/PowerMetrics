package com.BEPerfectSquat.BEPerfectSquat.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateVideoRequest {
    @NotNull(message = "sessionId no puede estar vacío")
    private Long sessionId;
    @NotBlank(message = "filePath no puede estar vacío")
    private String filePath;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
