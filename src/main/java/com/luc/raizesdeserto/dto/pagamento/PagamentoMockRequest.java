package com.luc.raizesdeserto.dto.pagamento;

import com.luc.raizesdeserto.domain.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record PagamentoMockRequest(
        @NotNull(message = "pedidoId é obrigatório.")
        UUID pedidoId,
        @NotNull(message = "statusPagamento é obrigatório")
        Status statusPagamento,
        @NotNull(message = "payload é obrigatório")
        String payloadWebhook
) {
}
