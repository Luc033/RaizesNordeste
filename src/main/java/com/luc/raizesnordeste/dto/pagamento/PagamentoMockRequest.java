package com.luc.raizesnordeste.dto.pagamento;

import com.luc.raizesnordeste.domain.enums.StatusPagamento;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PagamentoMockRequest(
        @NotNull(message = "pedidoId é obrigatório.")
        UUID pedidoId,
        @NotNull(message = "statusPagamento é obrigatório")
        StatusPagamento statusPagamento,
        @NotNull(message = "dateCreated é obrigatório")
        LocalDateTime dateCreated
) {
}

