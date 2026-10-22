package com.BEPerfectSquat.BEPerfectSquat.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.BEPerfectSquat.BEPerfectSquat.domain.enums.ExerciseType;
import com.BEPerfectSquat.BEPerfectSquat.domain.model.AnalysisParameters;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.client.dto.PythonAnalysisResponse;

class AnalysisClientTest {
    private MockRestServiceServer server;
    private AnalysisClient analysisClient;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        analysisClient = new AnalysisClient(restTemplate, "http://python");
    }

    @Test
    void sendsJumpParametersAndReadsJumpResults() {
        server.expect(requestTo("http://python/analyze"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.sessionId").value(1))
                .andExpect(jsonPath("$.exerciseType").value("VERTICAL_JUMP"))
                .andExpect(jsonPath("$.bodyWeightKg").value(80.0))
                .andExpect(jsonPath("$.heightCm").value(180.0))
                .andRespond(withSuccess("""
                        {"sessionId": 1, "exerciseType": "VERTICAL_JUMP", "videoFps": 30.0, "lowFpsWarning": true,
                         "totalJumps": 1, "bestJumpHeightCm": 30.66, "avgJumpHeightCm": 30.66, "peakPowerW": 3429.8,
                         "fatigueIndex": 0.0, "reps": [],
                         "jumps": [{"jumpNumber": 1, "flightTime": 0.5, "heightFlightCm": 30.66,
                                    "takeoffVelocity": 2.45, "minKneeAngle": 95.0, "countermovementDepthCm": 22.1,
                                    "heightDisplacementCm": 26.5, "peakPowerW": 3429.8}]}
                        """, MediaType.APPLICATION_JSON));

        PythonAnalysisResponse response = analysisClient.analyzeVideo(1L, "uploads/video.mp4",
                ExerciseType.VERTICAL_JUMP, new AnalysisParameters(null, 80.0, 180.0));

        server.verify();
        assertTrue(response.getLowFpsWarning());
        assertEquals(1, response.getTotalJumps());
        assertEquals(30.66, response.getJumps().get(0).getHeightFlightCm());
    }

    @Test
    void sendsSquatWeight() {
        server.expect(requestTo("http://python/analyze"))
                .andExpect(jsonPath("$.exerciseType").value("SQUAT"))
                .andExpect(jsonPath("$.weightKg").value(100.0))
                .andRespond(withSuccess("""
                        {"sessionId": 1, "totalReps": 3, "validReps": 2, "reps": []}
                        """, MediaType.APPLICATION_JSON));

        PythonAnalysisResponse response = analysisClient.analyzeVideo(1L, "uploads/video.mp4",
                ExerciseType.SQUAT, new AnalysisParameters(100.0, null, null));

        server.verify();
        assertEquals(3, response.getTotalReps());
    }
}
