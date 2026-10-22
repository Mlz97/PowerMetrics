package com.BEPerfectSquat.BEPerfectSquat.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.JumpDetail;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.Video;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.AnalysisSessionState;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.ExerciseType;
import com.BEPerfectSquat.BEPerfectSquat.domain.exception.AnalysisSessionNotFoundException;
import com.BEPerfectSquat.BEPerfectSquat.domain.repository.AnalysisSessionRepository;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto.PythonAnalysisResponse;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto.PythonJumpDetail;

@ExtendWith(MockitoExtension.class)
class AnalysisSessionServiceTest {
    @Mock
    private AnalysisSessionRepository analysisSessionRepository;

    @InjectMocks
    private AnalysisSessionService analysisSessionService;

    @Test
    void listSessionsReturnsNewestFirst() {
        AnalysisSession newest = new AnalysisSession();
        newest.setId(2L);
        AnalysisSession oldest = new AnalysisSession();
        oldest.setId(1L);
        when(analysisSessionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(newest, oldest));

        List<AnalysisSession> sessions = analysisSessionService.listSessions();

        assertEquals(List.of(newest, oldest), sessions);
    }

    @Test
    void createSessionStoresExerciseType() {
        when(analysisSessionRepository.save(any(AnalysisSession.class))).thenAnswer(inv -> inv.getArgument(0));

        AnalysisSession session = analysisSessionService.createSession(ExerciseType.VERTICAL_JUMP);

        assertEquals(ExerciseType.VERTICAL_JUMP, session.getExerciseType());
        assertEquals(AnalysisSessionState.WAITING, session.getState());
    }

    @Test
    void newSessionDefaultsToSquat() {
        assertEquals(ExerciseType.SQUAT, new AnalysisSession().getExerciseType());
    }

    @Test
    void updateSessionWithJumpResults() {
        AnalysisSession session = new AnalysisSession();
        session.setId(1L);
        session.setExerciseType(ExerciseType.VERTICAL_JUMP);
        session.setState(AnalysisSessionState.PROCESSING);
        Video video = new Video();
        session.setVideo(video);
        when(analysisSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(analysisSessionRepository.save(session)).thenReturn(session);

        PythonJumpDetail jump = new PythonJumpDetail();
        jump.setJumpNumber(1);
        jump.setFlightTime(0.5);
        jump.setHeightFlightCm(30.66);
        jump.setTakeoffVelocity(2.45);
        jump.setCountermovementDepthCm(22.1);
        PythonAnalysisResponse response = new PythonAnalysisResponse();
        response.setProcessedVideoFilePath("uploads/video_analyzed.mp4");
        response.setVideoFps(30.0);
        response.setLowFpsWarning(true);
        response.setTotalJumps(1);
        response.setBestJumpHeightCm(30.66);
        response.setAvgJumpHeightCm(30.66);
        response.setPeakPowerW(3429.8);
        response.setFatigueIndex(0.0);
        response.setJumps(List.of(jump));

        analysisSessionService.updateSessionWithResults(1L, response);

        assertEquals(AnalysisSessionState.FINISHED, session.getState());
        assertEquals(30.0, session.getVideoFps());
        assertTrue(session.getLowFpsWarning());
        assertEquals(1, session.getTotalJumps());
        assertEquals(30.66, session.getBestJumpHeightCm());
        assertEquals(3429.8, session.getPeakPowerW());
        assertNull(session.getTotalReps());
        assertEquals("uploads/video_analyzed.mp4", video.getProcessedFilePath());

        assertEquals(1, session.getJumpDetails().size());
        JumpDetail detail = session.getJumpDetails().get(0);
        assertEquals(30.66, detail.getHeightFlightCm());
        assertEquals(22.1, detail.getCountermovementDepthCm());
        assertEquals(session, detail.getSession());
    }

    @Test
    void markAsErrorSetsErrorState() {
        AnalysisSession session = new AnalysisSession();
        session.setId(1L);
        session.setState(AnalysisSessionState.PROCESSING);
        when(analysisSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        analysisSessionService.markAsError(1L);

        assertEquals(AnalysisSessionState.ERROR, session.getState());
        verify(analysisSessionRepository).save(session);
    }

    @Test
    void markAsErrorFailsForUnknownSession() {
        when(analysisSessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AnalysisSessionNotFoundException.class, () -> analysisSessionService.markAsError(99L));
    }
}
