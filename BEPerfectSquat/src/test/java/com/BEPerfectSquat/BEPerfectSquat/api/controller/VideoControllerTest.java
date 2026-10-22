package com.BEPerfectSquat.BEPerfectSquat.api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.mock.web.MockMultipartFile;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.Video;
import com.BEPerfectSquat.BEPerfectSquat.domain.service.VideoService;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.service.StorageService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VideoController.class)
public class VideoControllerTest {
    @MockBean
    private VideoService videoService;

    @MockBean
    private StorageService storageService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadThroughVideoEndpointIsNoLongerAvailable() throws Exception {
        // La subida se hace solo con POST /session/{id}/video
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());

        mockMvc.perform(multipart("/video")
                .file(file)
                .param("sessionId", "1"))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(videoService);
    }

    @Test
    void downloadWithoutProcessedVideoReturnsNotFound() throws Exception {
        when(videoService.getVideoBySessionId(1L)).thenReturn(new Video());

        mockMvc.perform(get("/video/download/1"))
                .andExpect(status().isNotFound());
    }
}
