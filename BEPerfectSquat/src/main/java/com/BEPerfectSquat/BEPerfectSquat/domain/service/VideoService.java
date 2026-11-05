package com.BEPerfectSquat.BEPerfectSquat.domain.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.Video;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.AnalysisSessionState;
import com.BEPerfectSquat.BEPerfectSquat.domain.exception.AnalysisBusyException;
import com.BEPerfectSquat.BEPerfectSquat.domain.exception.AnalysisSessionNotFoundException;
import com.BEPerfectSquat.BEPerfectSquat.domain.model.AnalysisParameters;
import com.BEPerfectSquat.BEPerfectSquat.domain.repository.AnalysisSessionRepository;
import com.BEPerfectSquat.BEPerfectSquat.domain.repository.VideoRepository;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.service.StorageException;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.service.StorageService;

@Service
public class VideoService {
    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

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

    public Video saveVideo(Long sessionId, String ownerId, MultipartFile file, AnalysisParameters parameters) {
        AnalysisSession session = findOwnedSession(sessionId, ownerId);
                
        if (session.getState() != AnalysisSessionState.WAITING) {
            throw new IllegalStateException(
                    "No se pueden subir videos cuando la sesión está en estado " + session.getState());
        }

        parameters.validate();

        // Storage local
        String localFilePath = storageService.store(file);

        //Processing y guardar Video
        session.setState(AnalysisSessionState.PROCESSING);
        session.setWeightKg(parameters.weightKg());
        session.setBodyWeightKg(parameters.bodyWeightKg());
        session.setHeightCm(parameters.heightCm());
        analysisSessionRepository.save(session);

        Video newVideo = new Video();
        newVideo.setFilePath(localFilePath);
        newVideo.setSession(session);
        newVideo.setUploadedAt(LocalDateTime.now());
        session.setVideo(newVideo);
        Video savedVideo = videoRepository.save(newVideo);

        // Análisis en segundo plano: la sesión pasa a FINISHED o ERROR al terminar
        try {
            analysisRunner.analyze(sessionId, localFilePath, session.getExerciseType(), parameters);
        } catch (TaskRejectedException e) {
            // Cola llena: se deshace la subida para que el usuario pueda reintentar con la misma sesión
            session.setState(AnalysisSessionState.WAITING);
            session.setVideo(null);
            videoRepository.delete(savedVideo);
            analysisSessionRepository.save(session);
            try {
                storageService.delete(localFilePath);
            } catch (StorageException deleteError) {
                // Un fichero huérfano lo borra la retención; el cliente debe recibir el 503, no un 500
                log.warn("Could not delete rejected upload {}", localFilePath, deleteError);
            }
            throw new AnalysisBusyException();
        }

        return savedVideo;
    }

    public Video getVideoBySessionId(Long sessionId, String ownerId) {
        findOwnedSession(sessionId, ownerId);

        return videoRepository.findBySession_Id(sessionId);
    }

    private AnalysisSession findOwnedSession(Long sessionId, String ownerId) {
        return analysisSessionRepository.findById(sessionId)
                .filter(session -> ownerId != null && ownerId.equals(session.getOwnerId()))
                .orElseThrow(() -> new AnalysisSessionNotFoundException(sessionId));
    }
}
