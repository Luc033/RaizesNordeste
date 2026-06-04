package com.luc.raizesdeserto.dto.pedido;

import com.luc.raizesdeserto.domain.entity.ItemPedido;
import com.luc.raizesdeserto.domain.entity.Pagamento;
import com.luc.raizesdeserto.domain.entity.Unidade;
import com.luc.raizesdeserto.domain.enums.CanalPedido;
import com.luc.raizesdeserto.domain.enums.FormaPagamento;
import com.luc.raizesdeserto.domain.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
public record CriarPedidoRequest(
        @NotNull(message = "Id do Canal do Pedido é obrigatório.")
        UUID canalPedidoId,

        @NotNull(message = "Id do Canal do Pedido é obrigatório.")
        UUID unidadeId,

        @NotEmpty(message = "O pedido deve conter pelo menos um item.")
        List<ItemPedidoRequest> itens,

        @NotBlank(message = "Forma de pagamento é obrigatória.")
        FormaPagamento formaPagamento
) {
}
