package com.luc.raizesnordeste.dto.pedido;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ItemPedidoRequest(

        @NotNull(message = "Id do produto não pode ser nulo.")
        UUID produtoId,

        @NotNull(message = "Quantide não pode ser nulo.")
        @Positive(message = "Quantidade deve ser maior que zero.")
        Integer quantidade
) {
}
