package com.luc.raizesnordeste.dto.pedido;

import com.luc.raizesnordeste.domain.entity.ItemPedido;
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
    public ItemPedidoResponse(ItemPedido i){
        this(i.getProduto().getId(), i.getProduto().getNome(), i.getQuantidade(), i.getProduto().getPrecoBase());
    }
}
