package com.luc.raizesdeserto.dto.error;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ErrorResponse(
        String error,
        String message,
        List<ErroDetalhe> details,
        LocalDateTime timestamp,
        String path,
        UUID requestId
) {
}
