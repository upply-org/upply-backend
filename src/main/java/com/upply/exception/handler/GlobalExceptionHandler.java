package com.upply.exception.handler;

import com.upply.exception.custom.BusinessLogicException;
import com.upply.exception.custom.OperationNotPermittedException;
import com.upply.exception.custom.ResourceNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Custom Exceptions ───────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found: {} | {} {}", ex.getMessage(), request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequestException(
            BusinessLogicException ex, HttpServletRequest request) {

        log.warn("Business logic error: {} | {} {}", ex.getMessage(), request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ExceptionResponse> handleOperationNotPermittedException(
            OperationNotPermittedException ex, HttpServletRequest request) {

        log.warn("Operation not permitted: {} | {} {}", ex.getMessage(), request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // ── Validation Errors ───────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation failed: {} | {} {}", errors, request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
    }

    // ── Request Errors ──────────────────────────────────────────────────

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        String message = String.format("Required parameter '%s' of type '%s' is missing",
                ex.getParameterName(), ex.getParameterType());
        log.warn("Missing parameter: {} | {} {}", message, request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("Parameter '%s' should be of type '%s'",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Type mismatch: {} | {} {}", message, request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Unreadable request body | {} {}", request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body", request);
    }

    // ── Security Exceptions ─────────────────────────────────────────────

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleLockedException(
            LockedException ex, HttpServletRequest request) {

        log.warn("Locked account access attempt | {} {}", request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Account is locked", request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleDisabledException(
            DisabledException ex, HttpServletRequest request) {

        log.warn("Disabled account access attempt | {} {}", request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Account is disabled", request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        log.warn("Bad credentials attempt | {} {}", request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        log.warn("Authentication failure: {} | {} {}", ex.getMessage(), request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication failed", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied | {} {}", request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied", request);
    }

    // ── Data Integrity Exceptions ────────────────────────────────────────

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("Data integrity violation | {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.CONFLICT, "A conflicting record already exists", request);
    }

    // ── Method Not Supported ────────────────────────────────────────────

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("Method not supported: {} | {} {}", ex.getMethod(), request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED,
                String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod()), request);
    }

    // ── Messaging Exceptions ────────────────────────────────────────────

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleMessagingException(
            MessagingException ex, HttpServletRequest request) {

        log.error("Email sending failed | {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email", request);
    }

    // ── Catch-All ───────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception | {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", request);
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private ResponseEntity<ExceptionResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        return buildResponse(status, message, request, null);
    }

    private ResponseEntity<ExceptionResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request,
            Map<String, String> validationErrors) {

        ExceptionResponse response = ExceptionResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
