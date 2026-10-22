package com.BEPerfectSquat.BEPerfectSquat.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.JumpDetail;
import com.BEPerfectSquat.BEPerfectSquat.domain.entity.RepDetail;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.AnalysisSessionState;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.ExerciseType;
import com.BEPerfectSquat.BEPerfectSquat.domain.exception.InvalidAnalysisParametersException;
import com.BEPerfectSquat.BEPerfectSquat.domain.model.AnalysisParameters;
import com.BEPerfectSquat.BEPerfectSquat.domain.service.AnalysisSessionService;
import com.BEPerfectSquat.BEPerfectSquat.domain.service.VideoService;
import com.BEPerfectSquat.BEPerfectSquat.infrastructure.service.StorageException;

@WebMvcTest(AnalysisSessionController.class)
class AnalysisSessionControllerTest {
    @MockBean
    private AnalysisSessionService analysisSessionService;

    @MockBean
    private VideoService videoService;

    @Autowired
    private MockMvc mockMvc;

    private AnalysisSession session(ExerciseType type, AnalysisSessionState state) {
        AnalysisSession session = new AnalysisSession();
        session.setId(1L);
        session.setExerciseType(type);
        session.setState(state);
        return session;
    }

    @Test
    void createSessionWithoutBodyDefaultsToSquat() throws Exception {
        when(analysisSessionService.createSession(ExerciseType.SQUAT))
                .thenReturn(session(ExerciseType.SQUAT, AnalysisSessionState.WAITING));

        mockMvc.perform(post("/session"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseType").value("SQUAT"));
    }

    @Test
    void createVerticalJumpSession() throws Exception {
        when(analysisSessionService.createSession(ExerciseType.VERTICAL_JUMP))
                .thenReturn(session(ExerciseType.VERTICAL_JUMP, AnalysisSessionState.WAITING));

        mockMvc.perform(post("/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"exerciseType\": \"VERTICAL_JUMP\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseType").value("VERTICAL_JUMP"))
                .andExpect(jsonPath("$.state").value("WAITING"));
    }

    @Test
    void createSessionRejectsUnknownExercise() throws Exception {
        mockMvc.perform(post("/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"exerciseType\": \"DEADLIFT\"}"))
                .andExpect(status().isBadRequest());

        verify(analysisSessionService, never()).createSession(any());
    }

    @Test
    void uploadVideoAcceptsAndReturnsProcessingSession() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());
        when(analysisSessionService.getSessionById(1L))
                .thenReturn(session(ExerciseType.SQUAT, AnalysisSessionState.PROCESSING));

        mockMvc.perform(multipart("/session/1/video")
                .file(file)
                .param("weightKg", "80.0"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.state").value("PROCESSING"));

        verify(videoService).saveVideo(eq(1L), any(), eq(new AnalysisParameters(80.0, null, null)));
    }

    @Test
    void uploadJumpVideoPassesBodyWeightAndHeight() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());
        when(analysisSessionService.getSessionById(1L))
                .thenReturn(session(ExerciseType.VERTICAL_JUMP, AnalysisSessionState.PROCESSING));

        mockMvc.perform(multipart("/session/1/video")
                .file(file)
                .param("bodyWeightKg", "80.0")
                .param("heightCm", "180.0"))
                .andExpect(status().isAccepted());

        verify(videoService).saveVideo(eq(1L), any(), eq(new AnalysisParameters(null, 80.0, 180.0)));
    }

    @Test
    void uploadVideoRejectsInvalidParameters() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());
        when(videoService.saveVideo(eq(1L), any(), any()))
                .thenThrow(new InvalidAnalysisParametersException("heightCm debe estar entre 100 y 250"));

        mockMvc.perform(multipart("/session/1/video")
                .file(file)
                .param("heightCm", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PARAMETERS"))
                .andExpect(jsonPath("$.message").value("heightCm debe estar entre 100 y 250"));
    }

    @Test
    void uploadVideoOnNonWaitingSessionReturnsConflictWithCause() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());
        when(videoService.saveVideo(eq(1L), any(), any()))
                .thenThrow(new IllegalStateException("No se pueden subir videos cuando la sesión está en estado FINISHED"));

        mockMvc.perform(multipart("/session/1/video").file(file))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_STATE"))
                .andExpect(jsonPath("$.message").value("No se pueden subir videos cuando la sesión está en estado FINISHED"));
    }

    @Test
    void uploadVideoBadArgumentReturnsItsMessage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());
        when(videoService.saveVideo(eq(1L), any(), any()))
                .thenThrow(new IllegalArgumentException("El archivo de vídeo está vacío"));

        mockMvc.perform(multipart("/session/1/video").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("El archivo de vídeo está vacío"));
    }

    @Test
    void uploadVideoStorageFailureReturnsStorageError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "content".getBytes());
        when(videoService.saveVideo(eq(1L), any(), any()))
                .thenThrow(new StorageException("Fallo al guardar el archivo."));

        mockMvc.perform(multipart("/session/1/video").file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("STORAGE_ERROR"))
                .andExpect(jsonPath("$.message").value("Fallo al guardar el archivo."));
    }

    @Test
    void getJumpSessionReturnsJumpMetrics() throws Exception {
        AnalysisSession session = session(ExerciseType.VERTICAL_JUMP, AnalysisSessionState.FINISHED);
        session.setBodyWeightKg(80.0);
        session.setHeightCm(180.0);
        session.setVideoFps(30.0);
        session.setLowFpsWarning(true);
        session.setTotalJumps(1);
        session.setBestJumpHeightCm(30.66);
        JumpDetail jump = new JumpDetail();
        jump.setJumpNumber(1);
        jump.setFlightTime(0.5);
        jump.setHeightFlightCm(30.66);
        session.getJumpDetails().add(jump);
        when(analysisSessionService.getSessionById(1L)).thenReturn(session);

        mockMvc.perform(get("/session/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciseType").value("VERTICAL_JUMP"))
                .andExpect(jsonPath("$.bodyWeightKg").value(80.0))
                .andExpect(jsonPath("$.heightCm").value(180.0))
                .andExpect(jsonPath("$.lowFpsWarning").value(true))
                .andExpect(jsonPath("$.totalJumps").value(1))
                .andExpect(jsonPath("$.bestJumpHeightCm").value(30.66))
                .andExpect(jsonPath("$.jumps[0].jumpNumber").value(1))
                .andExpect(jsonPath("$.jumps[0].heightFlightCm").value(30.66));
    }

    @Test
    void listSessionsReturnsAllSessionsInServiceOrder() throws Exception {
        AnalysisSession jump = session(ExerciseType.VERTICAL_JUMP, AnalysisSessionState.FINISHED);
        jump.setId(2L);
        AnalysisSession squat = session(ExerciseType.SQUAT, AnalysisSessionState.ERROR);
        when(analysisSessionService.listSessions()).thenReturn(List.of(jump, squat));

        mockMvc.perform(get("/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].exerciseType").value("VERTICAL_JUMP"))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].state").value("ERROR"));
    }

    @Test
    void listSessionsWithoutSessionsReturnsEmptyArray() throws Exception {
        when(analysisSessionService.listSessions()).thenReturn(List.of());

        mockMvc.perform(get("/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getSquatSessionReturnsRepDetailsAndDecimalWeight() throws Exception {
        AnalysisSession session = session(ExerciseType.SQUAT, AnalysisSessionState.FINISHED);
        session.setWeightKg(102.5);
        session.setTotalReps(1);
        RepDetail rep = new RepDetail();
        rep.setRepNumber(1);
        rep.setValid(true);
        rep.setMinHipAngle(70.0);
        rep.setMinKneeAngle(65.0);
        rep.setConcentricVelocity(0.8);
        session.getRepDetails().add(rep);
        when(analysisSessionService.getSessionById(1L)).thenReturn(session);

        mockMvc.perform(get("/session/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weightKg").value(102.5))
                .andExpect(jsonPath("$.reps[0].repNumber").value(1))
                .andExpect(jsonPath("$.reps[0].valid").value(true))
                .andExpect(jsonPath("$.reps[0].minHipAngle").value(70.0))
                .andExpect(jsonPath("$.reps[0].minKneeAngle").value(65.0))
                .andExpect(jsonPath("$.reps[0].concentricVelocity").value(0.8));
    }
}
