package by.shakhau.ps.auth.controller;

import by.shakhau.ps.auth.controller.dto.response.ErrorResponse;
import by.shakhau.ps.auth.controller.exception.UnauthorizedException;
import by.shakhau.ps.auth.service.exception.ResourceForbiddenException;
import by.shakhau.ps.auth.service.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<Class<? extends Exception>, HttpStatus> RESPONSE_STATUSES = new HashMap<>();

    static {
        RESPONSE_STATUSES.put(ResourceNotFoundException.class, HttpStatus.NOT_FOUND);
        RESPONSE_STATUSES.put(UnauthorizedException.class, HttpStatus.UNAUTHORIZED);
        RESPONSE_STATUSES.put(ResourceForbiddenException.class, HttpStatus.FORBIDDEN);
        RESPONSE_STATUSES.put(NonTransientDataAccessException.class, HttpStatus.FORBIDDEN);
        RESPONSE_STATUSES.put(MethodValidationException.class, HttpStatus.BAD_REQUEST);
        RESPONSE_STATUSES.put(MethodArgumentNotValidException.class, HttpStatus.BAD_REQUEST);
        RESPONSE_STATUSES.put(HandlerMethodValidationException.class, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        HttpStatus status = findHttpStatus(exception.getClass());

        if (status.is5xxServerError()) {
            log.error("Status: {}, request: {}, exception message: {}",
                    status.value(), request.getRequestURI(), exception.getMessage(), exception);
        } else {
            log.warn("Status: {}, request: {}, exception message: {}",
                    status.value(), request.getRequestURI(), exception.getMessage());
        }

        return buildErrorResponse(status, exception, request);
    }

    private HttpStatus findHttpStatus(Class<?> exceptionType) {
        while (exceptionType != null && exceptionType != Exception.class) {
            HttpStatus status = RESPONSE_STATUSES.get(exceptionType);
            if (status != null) {
                return status;
            }
            exceptionType = exceptionType.getSuperclass();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status, Exception exception, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}
