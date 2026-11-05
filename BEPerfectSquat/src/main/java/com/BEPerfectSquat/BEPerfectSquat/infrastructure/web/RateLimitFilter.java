package com.BEPerfectSquat.BEPerfectSquat.infrastructure.web;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import com.BEPerfectSquat.BEPerfectSquat.api.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Sin login cualquiera puede subir vídeos: se limita por IP la subida, la creación de sesiones y el resto del API
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {
    private static final Pattern UPLOAD_PATH = Pattern.compile("^/session/\\d+/video/?$");
    private static final Pattern SESSION_PATH = Pattern.compile("^/session/?$");

    private enum Rule { UPLOAD, CREATE, GENERAL }

    private final List<Bandwidth> uploadLimits;
    private final List<Bandwidth> createLimits;
    private final List<Bandwidth> generalLimits;
    private final ObjectMapper objectMapper;
    // Los buckets inactivos caducan para que la memoria no crezca con cada IP nueva
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofDays(1))
            .maximumSize(100_000)
            .build();

    public RateLimitFilter(
            @Value("${pwr.rate-limit.upload-per-hour:5}") long uploadPerHour,
            @Value("${pwr.rate-limit.upload-per-day:20}") long uploadPerDay,
            @Value("${pwr.rate-limit.create-per-hour:30}") long createPerHour,
            @Value("${pwr.rate-limit.general-per-minute:120}") long generalPerMinute,
            ObjectMapper objectMapper) {
        this.uploadLimits = List.of(limit(uploadPerHour, Duration.ofHours(1)), limit(uploadPerDay, Duration.ofDays(1)));
        this.createLimits = List.of(limit(createPerHour, Duration.ofHours(1)));
        this.generalLimits = List.of(limit(generalPerMinute, Duration.ofMinutes(1)));
        this.objectMapper = objectMapper;
    }

    private static Bandwidth limit(long capacity, Duration period) {
        return Bandwidth.builder().capacity(capacity).refillIntervally(capacity, period).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if ("OPTIONS".equals(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        Rule rule = ruleFor(request);
        // Con server.forward-headers-strategy=native, getRemoteAddr ya es la IP real del cliente detrás de Caddy
        String key = request.getRemoteAddr() + "|" + rule;
        Bucket bucket = buckets.get(key, ignored -> newBucket(rule));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            chain.doFilter(request, response);
            // Una subida rechazada por la cola llena (503) no gasta cupo: el cliente debe poder reintentar tras Retry-After
            if (rule == Rule.UPLOAD && response.getStatus() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
                bucket.addTokens(1);
            }
            return;
        }

        long retryAfterSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorResponse body = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "TOO_MANY_REQUESTS",
                "Demasiadas peticiones, inténtalo más tarde",
                LocalDateTime.now());
        objectMapper.writeValue(response.getWriter(), body);
    }

    private static Rule ruleFor(HttpServletRequest request) {
        // Ruta decodificada y sin parámetros ";x=1", la misma que usa Spring para enrutar: la URI cruda permitía saltarse el límite
        String path = UrlPathHelper.defaultInstance.getPathWithinApplication(request);
        if ("POST".equals(request.getMethod()) && UPLOAD_PATH.matcher(path).matches()) {
            return Rule.UPLOAD;
        }
        if ("POST".equals(request.getMethod()) && SESSION_PATH.matcher(path).matches()) {
            return Rule.CREATE;
        }
        return Rule.GENERAL;
    }

    private Bucket newBucket(Rule rule) {
        List<Bandwidth> limits = switch (rule) {
            case UPLOAD -> uploadLimits;
            case CREATE -> createLimits;
            case GENERAL -> generalLimits;
        };
        var builder = Bucket.builder();
        limits.forEach(builder::addLimit);
        return builder.build();
    }
}
