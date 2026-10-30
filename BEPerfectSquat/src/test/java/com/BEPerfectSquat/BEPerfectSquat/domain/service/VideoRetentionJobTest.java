package com.BEPerfectSquat.BEPerfectSquat.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.Video;
import com.BEPerfectSquat.BEPerfectSquat.domain.repository.VideoRepository;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.service.StorageException;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.service.StorageService;

@ExtendWith(MockitoExtension.class)
class VideoRetentionJobTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-30T03:00:00Z"), ZoneOffset.UTC);

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private StorageService storageService;

    private VideoRetentionJob job() {
        return new VideoRetentionJob(videoRepository, storageService, 7, CLOCK);
    }

    private Video video(String filePath, String processedPath) {
        Video video = new Video();
        video.setFilePath(filePath);
        video.setProcessedFilePath(processedPath);
        return video;
    }

    @Test
    void deletesFilesOlderThanRetentionAndKeepsTheRecord() {
        Video old = video("/videos/a.mov", "/videos/a_analyzed.mp4");
        when(videoRepository.findByUploadedAtBeforeAndFilePathIsNotNull(LocalDateTime.of(2026, 10, 23, 3, 0)))
                .thenReturn(List.of(old));

        int purged = job().purgeExpiredVideos();

        assertEquals(1, purged);
        verify(storageService).delete("/videos/a.mov");
        verify(storageService).delete("/videos/a_analyzed.mp4");
        assertNull(old.getFilePath());
        assertNull(old.getProcessedFilePath());
        verify(videoRepository).saveAll(List.of(old));
    }

    @Test
    void videoWithoutProcessedFileOnlyDeletesTheOriginal() {
        Video old = video("/videos/b.mp4", null);
        when(videoRepository.findByUploadedAtBeforeAndFilePathIsNotNull(any())).thenReturn(List.of(old));

        job().purgeExpiredVideos();

        verify(storageService).delete("/videos/b.mp4");
        assertNull(old.getFilePath());
    }

    @Test
    void oneFailingFileDoesNotStopThePurge() {
        Video broken = video("/videos/c.mp4", null);
        Video fine = video("/videos/d.mp4", null);
        when(videoRepository.findByUploadedAtBeforeAndFilePathIsNotNull(any())).thenReturn(List.of(broken, fine));
        doThrow(new StorageException("No se pudo borrar el archivo: /videos/c.mp4"))
                .when(storageService).delete("/videos/c.mp4");

        int purged = job().purgeExpiredVideos();

        assertEquals(1, purged);
        assertEquals("/videos/c.mp4", broken.getFilePath());
        assertNull(fine.getFilePath());
    }
}
