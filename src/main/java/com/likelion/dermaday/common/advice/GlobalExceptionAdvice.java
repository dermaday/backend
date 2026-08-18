package com.likelion.dermaday.common.advice;

import com.likelion.dermaday.common.exception.BaseException;
import com.likelion.dermaday.common.response.ApiResponse;
import com.likelion.dermaday.common.response.ErrorStatus;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException exception) {
        HttpStatus httpStatus = HttpStatus.valueOf(exception.getStatusCode());
        String message = exception.getResponseMessage() == null
                ? httpStatus.getReasonPhrase()
                : exception.getResponseMessage();

        return ResponseEntity
                .status(httpStatus)
                .body(ApiResponse.fail(exception.getStatusCode(), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        log.debug("Request validation failed", exception);
        ErrorStatus status = ErrorStatus.BAD_REQUEST_VALID_FAILED;
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(ApiResponse.failOnly(status));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(Exception exception) {
        log.debug("Invalid request", exception);
        ErrorStatus status = ErrorStatus.BAD_REQUEST_VALID_FAILED;
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(ApiResponse.failOnly(status));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(DataIntegrityViolationException exception) {
        log.debug("Data integrity violation", exception);
        ErrorStatus status = ErrorStatus.CONFLICT_DUPLICATE_RESOURCE;
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(ApiResponse.failOnly(status));
    }
}
