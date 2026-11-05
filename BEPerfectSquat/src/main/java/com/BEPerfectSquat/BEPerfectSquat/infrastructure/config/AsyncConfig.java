package com.BEPerfectSquat.BEPerfectSquat.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
    public static final String ANALYSIS_EXECUTOR = "analysisExecutor";

    // Pool acotado: cada análisis consume CPU durante segundos o minutos; con la cola llena se rechaza la subida (503)
    @Bean(name = ANALYSIS_EXECUTOR)
    public ThreadPoolTaskExecutor analysisExecutor(
            @Value("${pwr.analysis.concurrency:2}") int concurrency,
            @Value("${pwr.analysis.queue-capacity:10}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(concurrency);
        executor.setMaxPoolSize(concurrency);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("analysis-");
        executor.initialize();
        return executor;
    }
}
