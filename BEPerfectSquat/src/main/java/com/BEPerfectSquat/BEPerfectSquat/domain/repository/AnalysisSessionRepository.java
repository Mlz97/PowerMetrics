package com.BEPerfectSquat.BEPerfectSquat.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BEPerfectSquat.BEPerfectSquat.domain.entity.AnalysisSession;

public interface AnalysisSessionRepository extends JpaRepository<AnalysisSession, Long>{
    List<AnalysisSession> findAllByOrderByCreatedAtDesc();

}
