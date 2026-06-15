package com.luc.raizesdeserto.dto.pagamento;


import com.luc.raizesdeserto.domain.enums.Status;
import com.luc.raizesdeserto.domain.enums.StatusPagamento;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PagamentoResponse(
        UUID id,
        UUID pedidoId,
        UUID transacaoId,
        String payload,
        StatusPagamento status,
        LocalDateTime confirmedAt
) {
}
