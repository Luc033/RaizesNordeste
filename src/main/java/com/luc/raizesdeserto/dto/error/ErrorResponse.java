package com.luc.raizesdeserto.dto.error;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ErrorResponse(
        String error,
        String message,
        List<ErroDetalhe> details,
        LocalDateTime timestamp,
        String path,
        UUID requestId
) {
}
