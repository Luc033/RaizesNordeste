package com.luc.raizesdeserto.dto.pedido;

import com.luc.raizesdeserto.domain.enums.CanalPedido;
import com.luc.raizesdeserto.domain.enums.Status;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record PedidoResponse(
    UUID pedidoId,
    Status status,
    CanalPedido canal,
    BigDecimal valorTotal,
    List<ItemPedidoResponse> itens,
    LocalDateTime criadoEm
) {
}
