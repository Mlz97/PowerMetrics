package com.BEPerfectSquat.BEPerfectSquat.domain.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.Video;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.JumpDetail;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.AnalysisSessionState;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.ExerciseType;
import com.BEPerfectSquat.BEPerfectSquat.domain.exception.AnalysisSessionNotFoundException;
import com.BEPerfectSquat.BEPerfectSquat.domain.repository.AnalysisSessionRepository;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto.PythonJumpDetail;

@Service
public class AnalysisSessionService {
    private final AnalysisSessionRepository analysisSessionRepository;

    public AnalysisSessionService(AnalysisSessionRepository analysisSessionRepository) {
        this.analysisSessionRepository = analysisSessionRepository;
    }

    public AnalysisSession createSession(ExerciseType exerciseType) {
        AnalysisSession newSession = new AnalysisSession();
        newSession.setExerciseType(exerciseType);
        newSession.setState(AnalysisSessionState.WAITING);
        newSession.setCreatedAt(LocalDateTime.now());
        return analysisSessionRepository.save(newSession);
    }

    public AnalysisSession getSessionById(Long id) {
        AnalysisSession session = analysisSessionRepository.findById(id)
                .orElseThrow(() -> new AnalysisSessionNotFoundException(id));
        return session;
    }

    @Transactional
    public AnalysisSession updateSessionWithResults(Long id, com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto.PythonAnalysisResponse response) {
        AnalysisSession session = analysisSessionRepository.findById(id)
                .orElseThrow(() -> new AnalysisSessionNotFoundException(id));

        if (session.getState() != AnalysisSessionState.PROCESSING) {
            throw new IllegalStateException(
                    "Solo se pueden añadir resultados a sesiones en estado PROCESSING. Estado actual: "
                            + session.getState());
        }

        session.setTotalReps(response.getTotalReps());
        session.setValidReps(response.getValidReps());
        session.setAvgConcentricVelocity(response.getAverageConcentricVelocity());
        session.setEstimatedRM(response.getEstimatedRM());
        session.setFatigueIndex(response.getFatigueIndex());
        session.setVideoFps(response.getVideoFps());
        session.setLowFpsWarning(response.getLowFpsWarning());
        session.setTotalJumps(response.getTotalJumps());
        session.setBestJumpHeightCm(response.getBestJumpHeightCm());
        session.setAvgJumpHeightCm(response.getAvgJumpHeightCm());
        session.setPeakPowerW(response.getPeakPowerW());

        if (response.getReps() != null) {
            session.getRepDetails().clear();
            for (com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto.PythonRepDetail rep : response.getReps()) {
                com.BEPerfectSquat.BEPerfectSquat.domain.entity.RepDetail detail = new com.BEPerfectSquat.BEPerfectSquat.domain.entity.RepDetail();
                detail.setRepNumber(rep.getRepNumber());
                detail.setValid(rep.getValid());
                detail.setMinHipAngle(rep.getMinHipAngle());
                detail.setMinKneeAngle(rep.getMinKneeAngle());
                detail.setConcentricVelocity(rep.getConcentricVelocity());
                detail.setSession(session);
                session.getRepDetails().add(detail);
            }
        }

        if (response.getJumps() != null) {
            session.getJumpDetails().clear();
            for (PythonJumpDetail jump : response.getJumps()) {
                JumpDetail detail = new JumpDetail();
                detail.setJumpNumber(jump.getJumpNumber());
                detail.setFlightTime(jump.getFlightTime());
                detail.setHeightFlightCm(jump.getHeightFlightCm());
                detail.setTakeoffVelocity(jump.getTakeoffVelocity());
                detail.setMinKneeAngle(jump.getMinKneeAngle());
                detail.setCountermovementDepthCm(jump.getCountermovementDepthCm());
                detail.setHeightDisplacementCm(jump.getHeightDisplacementCm());
                detail.setPeakPowerW(jump.getPeakPowerW());
                detail.setSession(session);
                session.getJumpDetails().add(detail);
            }
        }

        session.setState(AnalysisSessionState.FINISHED);

        // Si ruta del vídeo procesado->actualizar el video asociado
        Video video = session.getVideo();
        if (video != null && response.getProcessedVideoFilePath() != null && !response.getProcessedVideoFilePath().isBlank()) {
            video.setProcessedFilePath(response.getProcessedVideoFilePath());
        }

        return analysisSessionRepository.save(session);
    }

    @Transactional
    public void markAsError(Long id) {
        AnalysisSession session = analysisSessionRepository.findById(id)
                .orElseThrow(() -> new AnalysisSessionNotFoundException(id));
        session.setState(AnalysisSessionState.ERROR);
        analysisSessionRepository.save(session);
    }
}
