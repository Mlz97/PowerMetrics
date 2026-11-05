package com.BEPerfectSquat.BEPerfectSquat.domain.exception;

// La cola de análisis está llena: se rechaza la subida en vez de acumular trabajo de CPU sin límite
public class AnalysisBusyException extends RuntimeException{
    public AnalysisBusyException(){
        super("El servidor está analizando demasiados vídeos, inténtalo en unos minutos");
    }
}
