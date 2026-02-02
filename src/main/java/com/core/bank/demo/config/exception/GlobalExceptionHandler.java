package com.core.bank.demo.config.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.core.bank.demo.contract.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());

        Response response = ex.getDetails() != null
                ? Response.error(ex.getErrorCode(), ex.getMessage(), ex.getDetails())
                : Response.error(ex.getErrorCode(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Response> handleSystemException(SystemException ex) {
        log.error("System exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage(), ex);

        Response response = Response.error(ex.getErrorCode(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation failed: {}", fieldErrors);

        Response response = Response.error(ErrorCode.VALIDATION_ERROR, "Validation failed for one or more fields",
                fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        Response response = Response.error(ErrorCode.ACCESS_DENIED,
                "You don't have permission to perform this operation");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Response> handleJwtException(JwtException ex) {
        log.warn("JWT exception: {}", ex.getMessage());

        Response response = Response.error(ErrorCode.INVALID_TOKEN, "Invalid or expired token");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        Response response = Response.error(ErrorCode.INVALID_REQUEST, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        Response response = Response.error(ErrorCode.SYSTEM_ERROR, cause.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
