package com.BEPerfectSquat.BEPerfectSquat.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;
import com.BEPerfectSquat.BEPerfectSquat.domain.enums.AnalysisSessionState;
import com.BEPerfectSquat.BEPerfectSquat.domain.exception.AnalysisSessionNotFoundException;
import com.BEPerfectSquat.BEPerfectSquat.domain.repository.AnalysisSessionRepository;

@ExtendWith(MockitoExtension.class)
class AnalysisSessionServiceTest {
    @Mock
    private AnalysisSessionRepository analysisSessionRepository;

    @InjectMocks
    private AnalysisSessionService analysisSessionService;

    @Test
    void markAsErrorSetsErrorState() {
        AnalysisSession session = new AnalysisSession();
        session.setId(1L);
        session.setState(AnalysisSessionState.PROCESSING);
        when(analysisSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        analysisSessionService.markAsError(1L);

        assertEquals(AnalysisSessionState.ERROR, session.getState());
        verify(analysisSessionRepository).save(session);
    }

    @Test
    void markAsErrorFailsForUnknownSession() {
        when(analysisSessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AnalysisSessionNotFoundException.class, () -> analysisSessionService.markAsError(99L));
    }
}
