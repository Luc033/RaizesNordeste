package com.luc.raizesdeserto.exception;

import com.luc.raizesdeserto.dto.error.ErroDetalhe;
import com.luc.raizesdeserto.dto.error.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandle {
    // MethodArgumentNotValidException - status code 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ){
        List<ErroDetalhe> detalhesErro = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> ErroDetalhe.builder()
                        .field(fieldError.getField())
                        .issue(fieldError.getDefaultMessage())
                        .build()
                ).toList();

        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Erro na validação dos campos da requisição.")
                .details(detalhesErro)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(UUID.randomUUID())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // IllegalArgumentException - status code 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(UUID.randomUUID())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // EntityNotFoundException - status code 404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(UUID.randomUUID())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // DataIntegrityViolationException - status code 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(UUID.randomUUID())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Exception - status code 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(ex.getMessage())
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(UUID.randomUUID())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }



}
