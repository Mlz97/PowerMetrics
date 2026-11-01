package com.BEPerfectSquat.BEPerfectSquat.domain.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.Video;
import com.BEPerfectSquat.BEPerfectSquat.domain.repository.VideoRepository;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.service.StorageException;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.service.StorageService;

// Los vídeos son datos personales y ocupan disco: se borran tras pwr.retention.days, las métricas de la sesión se conservan
@Service
public class VideoRetentionJob {
    private static final Logger log = LoggerFactory.getLogger(VideoRetentionJob.class);

    private final VideoRepository videoRepository;
    private final StorageService storageService;
    private final int retentionDays;
    private final Clock clock;

    @Autowired
    public VideoRetentionJob(VideoRepository videoRepository, StorageService storageService,
                             @Value("${pwr.retention.days:7}") int retentionDays) {
        this(videoRepository, storageService, retentionDays, Clock.systemDefaultZone());
    }

    VideoRetentionJob(VideoRepository videoRepository, StorageService storageService, int retentionDays, Clock clock) {
        this.videoRepository = videoRepository;
        this.storageService = storageService;
        this.retentionDays = retentionDays;
        this.clock = clock;
    }

    @Scheduled(cron = "${pwr.retention.cron:0 0 3 * * *}")
    public int purgeExpiredVideos() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minusDays(retentionDays);
        int purged = 0;
        for (Video video : videoRepository.findByUploadedAtBeforeAndFilePathIsNotNull(cutoff)) {
            try {
                // El anotado primero: si falla, el original y sus rutas siguen intactos y se reintenta entero
                if (video.getProcessedFilePath() != null) {
                    storageService.delete(video.getProcessedFilePath());
                    video.setProcessedFilePath(null);
                }
                storageService.delete(video.getFilePath());
                video.setFilePath(null);
            } catch (StorageException e) {
                // Se reintenta en la siguiente ejecución
                log.warn("Could not purge video {}", video.getId(), e);
                continue;
            }
            // Cada vídeo se guarda al momento para que la base de datos nunca apunte a ficheros ya borrados
            videoRepository.save(video);
            purged++;
        }
        log.info("Purged {} videos uploaded before {}", purged, cutoff);
        return purged;
    }
}
