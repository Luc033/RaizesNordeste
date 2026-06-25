package com.luc.raizesnordeste.dto.pedido;

import com.luc.raizesnordeste.domain.entity.Pedido;
import com.luc.raizesnordeste.domain.enums.CanalPedido;
import com.luc.raizesnordeste.domain.enums.Status;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record PedidoPagamentoResponse(
    UUID pedidoId,
    Status status,
    CanalPedido canal,
    BigDecimal valorTotal,
    List<ItemPedidoResponse> itens,
    String infoPagamento,
    LocalDateTime criadoEm
) {
    public PedidoPagamentoResponse(Pedido p, String infoPagamento ){
        this(p.getId(), p.getStatus(), p.getCanalPedido(), p.getValorTotal(), p.getItens().stream().map(i -> new ItemPedidoResponse(i)).toList(), infoPagamento,  p.getCriadoEm());
    }
}
