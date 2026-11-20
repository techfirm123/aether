package com.aether.platform.web;

import com.aether.platform.identity.application.InvalidCredentialsException;
import com.aether.platform.identity.application.InvalidRefreshTokenException;
import com.aether.platform.identity.application.InvalidVerificationTokenException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationFailure(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new TreeMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage() == null
                            ? "Invalid value"
                            : fieldError.getDefaultMessage());
        }

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                "One or more request fields are invalid.",
                "VALIDATION_FAILED",
                request);

        problem.setProperty("fieldErrors", fieldErrors);

        return problemResponse(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidVerificationToken(
            InvalidVerificationTokenException exception,
            HttpServletRequest request) {

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid verification token",
                "The verification token is invalid or has expired.",
                "INVALID_VERIFICATION_TOKEN",
                request);

        return problemResponse(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request) {

        ProblemDetail problem = createProblem(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed",
                "Email or password is incorrect, or the account is not active.",
                "INVALID_CREDENTIALS",
                request);

        return problemResponse(HttpStatus.UNAUTHORIZED, problem);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRefreshToken(
            InvalidRefreshTokenException exception,
            HttpServletRequest request) {

        ProblemDetail problem = createProblem(
                HttpStatus.UNAUTHORIZED,
                "Refresh token rejected",
                "The refresh token is invalid, expired or has been revoked.",
                "INVALID_REFRESH_TOKEN",
                request);

        return problemResponse(HttpStatus.UNAUTHORIZED, problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid request body",
                "The request body is missing or could not be parsed.",
                "INVALID_REQUEST_BODY",
                request);

        return problemResponse(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedFailure(
            RuntimeException exception,
            HttpServletRequest request) {

        String correlationId = currentCorrelationId();

        log.error(
                "Unhandled request failure. correlationId={}",
                correlationId,
                exception);

        ProblemDetail problem = createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "The request could not be completed.",
                "INTERNAL_ERROR",
                request);

        return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, problem);
    }

    private ProblemDetail createProblem(
            HttpStatus status,
            String title,
            String detail,
            String errorCode,
            HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);

        problem.setTitle(title);
        problem.setType(URI.create("about:blank"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", errorCode);
        problem.setProperty("correlationId", currentCorrelationId());
        problem.setProperty("timestamp", Instant.now().toString());

        return problem;
    }

    private ResponseEntity<ProblemDetail> problemResponse(
            HttpStatus status,
            ProblemDetail problem) {

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private String currentCorrelationId() {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);

        return correlationId == null
                ? "unavailable"
                : correlationId;
    }
}