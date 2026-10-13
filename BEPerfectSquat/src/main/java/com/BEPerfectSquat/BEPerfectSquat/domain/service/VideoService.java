package com.BEPerfectSquat.BEPerfectSquat.domain.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.Video;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.AnalysisSessionState;
import com.BEPerfectSquat.BEPerfectSquat.domain.exception.AnalysisSessionNotFoundException;
import com.BEPerfectSquat.BEPerfectSquat.domain.repository.AnalysisSessionRepository;
import com.BEPerfectSquat.BEPerfectSquat.domain.repository.VideoRepository;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.service.StorageService;

@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final AnalysisSessionRepository analysisSessionRepository;
    private final StorageService storageService;
    private final AnalysisRunner analysisRunner;

    public VideoService(VideoRepository videoRepository, 
                        AnalysisSessionRepository analysisSessionRepository,
                        StorageService storageService,
                        AnalysisRunner analysisRunner) {
        this.videoRepository = videoRepository;
        this.analysisSessionRepository = analysisSessionRepository;
        this.storageService = storageService;
        this.analysisRunner = analysisRunner;
    }

    public Video saveVideo(Long sessionId, MultipartFile file, Double weightKg) {
        AnalysisSession session = analysisSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AnalysisSessionNotFoundException(sessionId));
                
        if (session.getState() != AnalysisSessionState.WAITING) {
            throw new IllegalArgumentException(
                    "No se pueden subir videos cuando la sesión está en estado " + session.getState());
        }

        // Storage local
        String localFilePath = storageService.store(file);

        //Processing y guardar Video
        session.setState(AnalysisSessionState.PROCESSING);
        if (weightKg != null) {
            session.setWeightKg(weightKg.intValue());
        }
        analysisSessionRepository.save(session);

        Video newVideo = new Video();
        newVideo.setFilePath(localFilePath);
        newVideo.setSession(session);
        newVideo.setUploadedAt(LocalDateTime.now());
        session.setVideo(newVideo);
        Video savedVideo = videoRepository.save(newVideo);

        // Análisis en segundo plano: la sesión pasa a FINISHED o ERROR al terminar
        analysisRunner.analyze(sessionId, localFilePath, weightKg);

        return savedVideo;
    }

    public Video getVideoBySessionId(Long sessionId) {
        analysisSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AnalysisSessionNotFoundException(sessionId));

        return videoRepository.findBySession_Id(sessionId);
    }
}
