package com.BEPerfectSquat.BEPerfectSquat.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "video")
// Modelo de dominio + persistencia(JPA)
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id")
    private AnalysisSession session;

    private LocalDateTime uploadedAt;

    private String filePath;

    @Column(name = "processed_file_path")
    private String processedFilePath;

    public AnalysisSession getSession() {
        return session;
    }

    public void setSession(AnalysisSession session) {
        this.session = session;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getProcessedFilePath() {
        return processedFilePath;
    }

    public void setProcessedFilePath(String processedFilePath) {
        this.processedFilePath = processedFilePath;
    }

}
