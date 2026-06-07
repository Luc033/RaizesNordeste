package com.luc.raizesdeserto.dto.estoque;

import com.luc.raizesdeserto.domain.enums.MovimentacaoTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record MovimentacaoRequest(
        @NotNull(message = "ID do produto não pode ser nulo.")
        UUID produtoID,

        @NotNull(message = "ID da unidade não pode ser nulo.")
        UUID unidadeId,

        @Positive(message = "Quantidade deve ser maior que zero.")
        @NotNull(message = "Quantidade não pode ser nula.")
        Integer quantidade,

        @NotBlank(message = "Tipo de movimentação não pode ser nulo.")
        MovimentacaoTipo tipo
) {
}
