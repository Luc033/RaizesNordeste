package com.luc.raizesdeserto.exception;

import com.luc.raizesdeserto.dto.error.ErroDetalhe;
import com.luc.raizesdeserto.dto.error.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // MethodArgumentNotValidException - status code 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ){
        UUID requestId = UUID.randomUUID();

        log.error("Erro inesperado - requestId: {} | path: {} | erro: {}",
                requestId, request.getRequestURI(), ex.getMessage(), ex);
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
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // IllegalArgumentException - status code 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        UUID requestId = UUID.randomUUID();

        log.error("Erro inesperado - requestId: {} | path: {} | erro: {}",
                requestId, request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

    }
    // TransactionSystemException  - status code 400
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ErrorResponse> handleTransactionSystemException(
            TransactionSystemException ex, HttpServletRequest request) {
        UUID requestId = UUID.randomUUID();

        log.error("Erro inesperado - requestId: {} | path: {} | erro: {}",
                requestId, request.getRequestURI(), ex.getMessage(), ex);

        Throwable cause = ex.getRootCause();
        if (cause instanceof ConstraintViolationException cve) {
            List<ErroDetalhe> detalhes = cve.getConstraintViolations().stream()
                    .map(v -> ErroDetalhe.builder()
                            .field(v.getPropertyPath().toString())
                            .issue(v.getMessage())
                            .build())
                    .toList();

            ErrorResponse response = ErrorResponse.builder()
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message("Erro de validação nos dados da entidade.")
                    .details(detalhes)
                    .timestamp(LocalDateTime.now())
                    .path(request.getRequestURI())
                    .requestId(requestId)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // fallback
        return handleException(ex, request);
    }

    // JSON mal formatado - HttpMessageNotReadableException - status code 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableExceptionException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        UUID requestId = UUID.randomUUID();

        log.error("Erro inesperado - requestId: {} | path: {} | erro: {}",
                requestId, request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("O corpo da requisição está ausente ou o formato do JSON enviado é inválido.")
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // AuthenticationException - status code 401
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex,
                                                                       HttpServletRequest request) {
        UUID requestId = UUID.randomUUID();

        log.error("Erro inesperado - requestId: {} | path: {} | erro: {}",
                requestId, request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(ex.getMessage())
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // AccessDeniedException - status code 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        UUID requestId = UUID.randomUUID();

        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("Você não tem permissão para acessar este recurso.")
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();
        log.error("Erro inesperado - requestId: {} | path: {} | erro: {}",
                requestId, request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }


    // EntityNotFoundException - status code 404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        UUID requestId = UUID.randomUUID();

        log.error("Erro inesperado - requestId: {} | path: {} | erro: {}",
                requestId, request.getRequestURI(), ex.getMessage(), ex);


        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // MethodNotAllowedException / NoResourceFoundException - status code 405
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        UUID requestId = UUID.randomUUID();

        log.error("Erro inesperado - requestId: {} | path: {} | erro: {}",
                requestId, request.getRequestURI(), ex.getMessage(), ex);


        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
                .message("Método HTTP não suportado para este endpoint.")
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // DataIntegrityViolationException - status code 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        UUID requestId = UUID.randomUUID();

        log.error("Erro inesperado - requestId: {} | path: {} | erro: {}",
                requestId, request.getRequestURI(), ex.getMessage(), ex);


        String message = "Operação viola restrição de integridade dos dados.";

        Throwable cause = ex.getMostSpecificCause();
        String causeMsg = cause.getMessage().toLowerCase();

        if (causeMsg.contains("uk_") || causeMsg.contains("unique")) {
            message = "Já existe um registro com esses dados.";
        } else if (causeMsg.contains("fk_") || causeMsg.contains("foreign key")) {
            message = "Referência a um recurso inexistente.";
        } else if (causeMsg.contains("not-null") || causeMsg.contains("null value")) {
            message = "Campo obrigatório ausente.";
        }
        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(message)
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // OptimisticLockingFailureException - status code 409
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(
            OptimisticLockingFailureException ex, HttpServletRequest request) {
        UUID requestId = UUID.randomUUID();

        log.error("Erro inesperado - requestId: {} | path: {} | erro: {}",
                requestId, request.getRequestURI(), ex.getMessage(), ex);


        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message("O recurso foi modificado por outra operação. Tente novamente.")
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }


    // Exception - status code 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        UUID requestId = UUID.randomUUID();

        log.error("Erro inesperado - requestId: {} | path: {} | erro: {}",
                requestId, request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Ocorreu um erro interno. Tente novamente ou contate o suporte.")
                .details(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
