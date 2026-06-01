package com.luc.raizesdeserto.dto.pedido;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ItemPedidoResponse(
        UUID produtoID,
        String nome,
        Integer quantidade,
        BigDecimal valorUnitario
) {
}
