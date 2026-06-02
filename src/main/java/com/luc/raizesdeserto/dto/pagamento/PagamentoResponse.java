package com.luc.raizesdeserto.dto.pagamento;


import com.luc.raizesdeserto.domain.enums.Status;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PagamentoResponse(
        UUID id,
        Status status,
        UUID transacaoId,
        LocalDateTime confirmedAt
) {
}
