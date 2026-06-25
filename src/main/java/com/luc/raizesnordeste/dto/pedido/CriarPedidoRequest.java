package com.luc.raizesnordeste.dto.pedido;

import com.luc.raizesnordeste.domain.enums.CanalPedido;
import com.luc.raizesnordeste.domain.enums.FormaPagamento;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CriarPedidoRequest(
        @NotNull(message = "Id do Canal do Pedido é obrigatório.")
        UUID unidadeId,

        @NotNull(message = "Canal do Pedido é obrigatório.")
        CanalPedido canalPedido,

        @NotEmpty(message = "O pedido deve conter pelo menos um item.")
        List<ItemPedidoRequest> itens,

        @NotNull(message = "Forma de pagamento é obrigatória.")
        FormaPagamento formaPagamento
) {
}
