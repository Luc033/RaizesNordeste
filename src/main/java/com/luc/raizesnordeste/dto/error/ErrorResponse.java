package com.luc.raizesnordeste.dto.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Schema(name = "ErrorResponse")
public class ErrorResponse {

    @Schema(example = "Bad Request")
    @JsonProperty("error")
    private String error;

    @Schema(example = "Erro na validação dos campos da requisição.")
    @JsonProperty("message")
    private String message;

    @JsonProperty("details")
    private List<ErroDetalhe> details;

    @Schema(example = "2026-01-15T10:30:00")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @Schema(example = "/pedidos")
    @JsonProperty("path")
    private String path;

    @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("requestId")
    private UUID requestId;
}