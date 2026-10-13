package com.BEPerfectSquat.BEPerfectSquat.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.AnalysisSessionState;
import com.BEPerfectSquat.BEPerfectSquat.domain.service.AnalysisSessionService;
import com.BEPerfectSquat.BEPerfectSquat.domain.service.VideoService;

@WebMvcTest(AnalysisSessionController.class)
class AnalysisSessionControllerTest {
    @MockBean
    private AnalysisSessionService analysisSessionService;

    @MockBean
    private VideoService videoService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadVideoAcceptsAndReturnsProcessingSession() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());
        AnalysisSession session = new AnalysisSession();
        session.setId(1L);
        session.setState(AnalysisSessionState.PROCESSING);
        when(analysisSessionService.getSessionById(1L)).thenReturn(session);

        mockMvc.perform(multipart("/session/1/video")
                .file(file)
                .param("weightKg", "80.0"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.state").value("PROCESSING"));

        verify(videoService).saveVideo(eq(1L), any(), eq(80.0));
    }
}
