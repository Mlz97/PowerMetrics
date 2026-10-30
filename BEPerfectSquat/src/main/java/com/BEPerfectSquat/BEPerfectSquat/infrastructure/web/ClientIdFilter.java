package com.BEPerfectSquat.BEPerfectSquat.infrastructure.web;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Sin login: cada navegador recibe un id anónimo en cookie y solo ve sus propias sesiones
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ClientIdFilter extends OncePerRequestFilter {
    public static final String COOKIE_NAME = "pwr_cid";
    public static final String ATTRIBUTE = "pwr.clientId";

    private final boolean secureCookie;

    public ClientIdFilter(@Value("${pwr.cookie.secure:false}") boolean secureCookie) {
        this.secureCookie = secureCookie;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String clientId = readClientId(request);
        if (clientId == null) {
            clientId = UUID.randomUUID().toString();
            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, clientId)
                    .httpOnly(true)
                    .secure(secureCookie)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofDays(365))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        request.setAttribute(ATTRIBUTE, clientId);
        chain.doFilter(request, response);
    }

    private static String readClientId(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(ClientIdFilter::isUuid)
                .findFirst()
                .orElse(null);
    }

    private static boolean isUuid(String value) {
        try {
            return UUID.fromString(value).toString().equals(value);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
