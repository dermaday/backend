package com.likelion.dermaday.common.advice;

import com.likelion.dermaday.common.exception.BaseException;
import com.likelion.dermaday.common.response.ApiResponse;
import com.likelion.dermaday.common.response.ErrorStatus;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
}
