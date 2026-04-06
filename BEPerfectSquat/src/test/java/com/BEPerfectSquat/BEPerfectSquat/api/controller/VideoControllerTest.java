package com.BEPerfectSquat.BEPerfectSquat.api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.Video;
import com.BEPerfectSquat.BEPerfectSquat.domain.exception.AnalysisSessionNotFoundException;
import com.BEPerfectSquat.BEPerfectSquat.domain.service.VideoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VideoController.class)
public class VideoControllerTest {
    @MockBean
    private VideoService videoService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createVideoSuccesfully() throws Exception {
        // GIVEN
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());

        AnalysisSession session = new AnalysisSession();
        session.setId(1L);

        Video video = new Video();
        video.setId(10L);
        video.setSession(session);
        video.setFilePath("uploads/video.mp4");

        when(videoService.saveVideo(eq(1L), any(), eq(50.0))).thenReturn(video);

        // WHEN + THEN
        mockMvc.perform(multipart("/video")
                .file(file)
                .param("sessionId", "1")
                .param("weightKg", "50.0"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(1L))
                .andExpect(jsonPath("$.filePath").value("uploads/video.mp4"));
    }

    @Test
    void createVideoValidationError() throws Exception {
        // GIVEN no file
        
        // WHEN + THEN
        mockMvc.perform(multipart("/video")
                .param("sessionId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createVideoSessionNotFound() throws Exception {
        // GIVEN
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());

        when(videoService.saveVideo(eq(99L), any(), eq(50.0)))
                .thenThrow(new AnalysisSessionNotFoundException(99L));

        // WHEN + THEN
        mockMvc.perform(multipart("/video")
                .file(file)
                .param("sessionId", "99")
                .param("weightKg", "50.0"))
                .andExpect(status().isNotFound());
    }
}
