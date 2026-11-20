package com.aether.platform.identity.security;

import com.aether.platform.web.CorrelationIdFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

@Component
public class SecurityAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    public SecurityAuthenticationEntryPoint(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication is required to access this resource.");

        problem.setTitle("Authentication required");
        problem.setType(URI.create("about:blank"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "AUTHENTICATION_REQUIRED");
        problem.setProperty(
                "correlationId",
                currentCorrelationId());
        problem.setProperty("timestamp", Instant.now().toString());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader(
                HttpHeaders.WWW_AUTHENTICATE,
                "Bearer realm=\"aether\"");

        jsonMapper.writeValue(response.getOutputStream(), problem);
    }

    private String currentCorrelationId() {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);

        return correlationId == null
                ? "unavailable"
                : correlationId;
    }
}