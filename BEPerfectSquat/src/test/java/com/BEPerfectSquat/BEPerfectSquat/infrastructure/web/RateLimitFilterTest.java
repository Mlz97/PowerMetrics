package com.BEPerfectSquat.BEPerfectSquat.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.BEPerfectSquat.BEPerfectSquat.api.controller.AnalysisSessionController;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.service.AnalysisSessionService;
import com.BEPerfectSquat.BEPerfectSquat.domain.service.VideoService;

@WebMvcTest(AnalysisSessionController.class)
@TestPropertySource(properties = {
        "pwr.rate-limit.upload-per-hour=2",
        "pwr.rate-limit.upload-per-day=20",
        "pwr.rate-limit.create-per-hour=3",
        "pwr.rate-limit.general-per-minute=4"
})
class RateLimitFilterTest {
    @MockBean
    private AnalysisSessionService analysisSessionService;

    @MockBean
    private VideoService videoService;

    @Autowired
    private MockMvc mockMvc;

    // Cada test usa su propia IP para no compartir buckets con los demás
    private static RequestPostProcessor fromIp(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    private MockMultipartFile video() {
        return new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());
    }

    @Test
    void uploadsOverTheHourlyLimitAreRejectedWithRetryAfter() throws Exception {
        when(analysisSessionService.getSessionById(eq(1L), anyString())).thenReturn(new AnalysisSession());

        mockMvc.perform(multipart("/session/1/video").file(video()).with(fromIp("10.0.0.1")))
                .andExpect(status().isAccepted());
        mockMvc.perform(multipart("/session/1/video").file(video()).with(fromIp("10.0.0.1")))
                .andExpect(status().isAccepted());
        mockMvc.perform(multipart("/session/1/video").file(video()).with(fromIp("10.0.0.1")))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("TOO_MANY_REQUESTS"))
                .andExpect(jsonPath("$.message").value("Demasiadas peticiones, inténtalo más tarde"));
    }

    @Test
    void pathParametersAndPercentEncodingStillCountAsUploads() throws Exception {
        when(analysisSessionService.getSessionById(eq(1L), anyString())).thenReturn(new AnalysisSession());

        mockMvc.perform(multipart("/session/1/video;x=1").file(video()).with(fromIp("10.0.0.8")))
                .andExpect(status().isAccepted());
        mockMvc.perform(multipart(URI.create("/session/1/vide%6F")).file(video()).with(fromIp("10.0.0.8")))
                .andExpect(status().isAccepted());
        mockMvc.perform(multipart("/session/1/video;jsessionid=abc").file(video()).with(fromIp("10.0.0.8")))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void sessionCreationWithPathParametersCountsAsCreation() throws Exception {
        when(analysisSessionService.createSession(any(), anyString())).thenReturn(new AnalysisSession());

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/session;x=" + i).with(fromIp("10.0.0.9")));
        }
        mockMvc.perform(post("/session").with(fromIp("10.0.0.9")))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void limitsAreCountedPerIp() throws Exception {
        when(analysisSessionService.getSessionById(eq(1L), anyString())).thenReturn(new AnalysisSession());

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(multipart("/session/1/video").file(video()).with(fromIp("10.0.0.2")))
                    .andExpect(status().isAccepted());
        }
        mockMvc.perform(multipart("/session/1/video").file(video()).with(fromIp("10.0.0.3")))
                .andExpect(status().isAccepted());
    }

    @Test
    void exhaustedUploadLimitDoesNotBlockReads() throws Exception {
        when(analysisSessionService.getSessionById(eq(1L), anyString())).thenReturn(new AnalysisSession());
        when(analysisSessionService.listSessions(anyString())).thenReturn(List.of());

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(multipart("/session/1/video").file(video()).with(fromIp("10.0.0.4")));
        }
        mockMvc.perform(get("/session").with(fromIp("10.0.0.4")))
                .andExpect(status().isOk());
    }

    @Test
    void sessionCreationHasItsOwnLimit() throws Exception {
        when(analysisSessionService.createSession(any(), anyString())).thenReturn(new AnalysisSession());

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/session").with(fromIp("10.0.0.5")))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/session").with(fromIp("10.0.0.5")))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void generalRequestsHaveAPerMinuteLimit() throws Exception {
        when(analysisSessionService.listSessions(anyString())).thenReturn(List.of());

        for (int i = 0; i < 4; i++) {
            mockMvc.perform(get("/session").with(fromIp("10.0.0.6")))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(get("/session").with(fromIp("10.0.0.6")))
                .andExpect(status().isTooManyRequests());
    }
}
