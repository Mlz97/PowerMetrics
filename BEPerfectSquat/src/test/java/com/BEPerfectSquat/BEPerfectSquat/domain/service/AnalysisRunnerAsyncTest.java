package com.BEPerfectSquat.BEPerfectSquat.domain.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.BEPerfectSquat.BEPerfectSquat.domain.enums.ExerciseType;
import com.BEPerfectSquat.BEPerfectSquat.domain.model.AnalysisParameters;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.AnalysisClient;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto.PythonAnalysisResponse;

@SpringBootTest
class AnalysisRunnerAsyncTest {
    @MockBean
    private AnalysisClient analysisClient;

    @MockBean
    private AnalysisSessionService analysisSessionService;

    @Autowired
    private AnalysisRunner analysisRunner;

    @Test
    void analysisRunsOutsideTheCallerThread() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<String> analysisThread = new AtomicReference<>();
        when(analysisClient.analyzeVideo(any(), any(), any(), any())).thenAnswer(invocation -> {
            analysisThread.set(Thread.currentThread().getName());
            done.countDown();
            return new PythonAnalysisResponse();
        });

        analysisRunner.analyze(1L, "uploads/video.mp4", ExerciseType.SQUAT, AnalysisParameters.empty());

        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertNotEquals(Thread.currentThread().getName(), analysisThread.get());
    }
}
