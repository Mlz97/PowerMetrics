package com.BEPerfectSquat.BEPerfectSquat.domain.exception;

public class AnalysisSessionNotFoundException extends RuntimeException{
    public AnalysisSessionNotFoundException(Long sessionId){
        super("Sesión de análisis con ID "+sessionId+" no encontrada");
    }
}
