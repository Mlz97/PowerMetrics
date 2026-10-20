package com.BEPerfectSquat.BEPerfectSquat.domain.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.Video;
import com.BEPerfectSquat.BEPerfectSquat.domain.exception.AnalysisSessionNotFoundException;
import com.BEPerfectSquat.BEPerfectSquat.domain.repository.AnalysisSessionRepository;
import com.BEPerfectSquat.BEPerfectSquat.domain.repository.VideoRepository;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.AnalysisSessionState;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.ExerciseType;
import com.BEPerfectSquat.BEPerfectSquat.domain.exception.InvalidAnalysisParametersException;
import com.BEPerfectSquat.BEPerfectSquat.domain.model.AnalysisParameters;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.service.StorageService;

@ExtendWith(MockitoExtension.class)
public class VideoServiceTest {
    @Mock
    private VideoRepository videoRepository;

    @Mock
    private AnalysisSessionRepository analysisSessionRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private AnalysisRunner analysisRunner;

    @InjectMocks
    private VideoService videoService;

    @Test
    void saveVideoOk() {
        // GIVEN
        Long sessionId = 1L;
        Double weightKg = 80.0;
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());

        AnalysisSession session = new AnalysisSession();
        session.setId(sessionId);

        Video saveVideo = new Video();
        saveVideo.setSession(session);
        saveVideo.setFilePath("uploads/video.mp4");

        when(analysisSessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(storageService.store(file)).thenReturn("uploads/video.mp4");

        when(videoRepository.save(any(Video.class)))
                .thenReturn(saveVideo);

        // WHEN
        AnalysisParameters params = new AnalysisParameters(weightKg, null, null);
        Video result = videoService.saveVideo(sessionId, file, params);

        // THEN
        assertNotNull(result);
        assertEquals("uploads/video.mp4", result.getFilePath());

        verify(storageService).store(file);
        verify(analysisSessionRepository).save(session);
        verify(videoRepository).save(any(Video.class));
        assertEquals(AnalysisSessionState.PROCESSING, session.getState());
        verify(analysisRunner).analyze(sessionId, "uploads/video.mp4", ExerciseType.SQUAT, params);
    }

    @Test
    void exceptionWhenSessionNotExists() {
        // GIVEN
        Long sessionId = 99L;
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());

        when(analysisSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(AnalysisSessionNotFoundException.class,
                () -> videoService.saveVideo(sessionId, file, AnalysisParameters.empty()));

        verify(videoRepository, never()).save(any());
        verify(storageService, never()).store(any());
    }

    @Test
    void rejectsUploadWhenSessionIsNotWaiting() {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());
        AnalysisSession session = new AnalysisSession();
        session.setId(1L);
        session.setState(AnalysisSessionState.PROCESSING);
        when(analysisSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> videoService.saveVideo(1L, file, AnalysisParameters.empty()));

        assertEquals("No se pueden subir videos cuando la sesión está en estado PROCESSING", ex.getMessage());
        verify(storageService, never()).store(any());
    }

    @Test
    void saveJumpVideoStoresJumpParameters() {
        Long sessionId = 2L;
        MockMultipartFile file = new MockMultipartFile("file", "jump.mp4", "video/mp4", "content".getBytes());
        AnalysisSession session = new AnalysisSession();
        session.setId(sessionId);
        session.setExerciseType(ExerciseType.VERTICAL_JUMP);
        AnalysisParameters params = new AnalysisParameters(null, 80.0, 180.0);

        when(analysisSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(storageService.store(file)).thenReturn("uploads/jump.mp4");
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        videoService.saveVideo(sessionId, file, params);

        assertEquals(80.0, session.getBodyWeightKg());
        assertEquals(180.0, session.getHeightCm());
        assertEquals(AnalysisSessionState.PROCESSING, session.getState());
        verify(analysisRunner).analyze(sessionId, "uploads/jump.mp4", ExerciseType.VERTICAL_JUMP, params);
    }

    @Test
    void rejectsInvalidParametersBeforeStoringVideo() {
        Long sessionId = 2L;
        MockMultipartFile file = new MockMultipartFile("file", "jump.mp4", "video/mp4", "content".getBytes());
        AnalysisSession session = new AnalysisSession();
        session.setId(sessionId);
        when(analysisSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(InvalidAnalysisParametersException.class,
                () -> videoService.saveVideo(sessionId, file, new AnalysisParameters(null, 80.0, 50.0)));

        verify(storageService, never()).store(any());
        assertEquals(AnalysisSessionState.WAITING, session.getState());
    }

    @Test
    void getVideoBySessionId() {
        // GIVEN
        Long sessionId = 1L;

        AnalysisSession session = new AnalysisSession();
        session.setId(sessionId);

        Video video = new Video();
        video.setSession(session);
        video.setFilePath("video.mp4");

        when(analysisSessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(videoRepository.findBySession_Id(sessionId))
                .thenReturn(video);

        // WHEN
        Video result = videoService.getVideoBySessionId(sessionId);

        // THEN
        assertNotNull(result);
        assertEquals("video.mp4", result.getFilePath());
        assertEquals(sessionId, result.getSession().getId());
        verify(videoRepository).findBySession_Id(sessionId);
    }

    @Test
    void exceptionGetVideoNonExistingSession() {
        // GIVEN
        Long sessionId = 99L;

        when(analysisSessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(
                AnalysisSessionNotFoundException.class,
                () -> videoService.getVideoBySessionId(sessionId));

        verify(videoRepository, never()).findBySession_Id(any());
    }
}
