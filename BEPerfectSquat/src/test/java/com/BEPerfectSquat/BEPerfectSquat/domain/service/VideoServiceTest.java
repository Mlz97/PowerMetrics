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
        Video result = videoService.saveVideo(sessionId, file, weightKg);

        // THEN
        assertNotNull(result);
        assertEquals("uploads/video.mp4", result.getFilePath());

        verify(storageService).store(file);
        verify(analysisSessionRepository).save(session);
        verify(videoRepository).save(any(Video.class));
        assertEquals(AnalysisSessionState.PROCESSING, session.getState());
        verify(analysisRunner).analyze(sessionId, "uploads/video.mp4", weightKg);
    }

    @Test
    void exceptionWhenSessionNotExists() {
        // GIVEN
        Long sessionId = 99L;
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());

        when(analysisSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(AnalysisSessionNotFoundException.class, () -> videoService.saveVideo(sessionId, file, null));

        verify(videoRepository, never()).save(any());
        verify(storageService, never()).store(any());
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
