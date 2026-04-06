package com.BEPerfectSquat.BEPerfectSquat.api.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> details;
    
    public ErrorResponse(int status, String error, String message, LocalDateTime timestamp, Map<String, String> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.details = details;
    }
    public ErrorResponse(int status, String error, String message, LocalDateTime timestamp){
        this(status, error, message, timestamp, null);
    }
    public int getStatus() {
        return status;
    }
    public String getError() {
        return error;
    }
    public String getMessage() {
        return message;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public Map<String, String> getDetails() {
        return details;
    }
    
}
