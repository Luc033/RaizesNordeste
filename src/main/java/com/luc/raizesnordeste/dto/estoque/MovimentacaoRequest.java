package com.luc.raizesnordeste.dto.estoque;

import com.luc.raizesnordeste.domain.enums.MovimentacaoTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record MovimentacaoRequest(
        @NotNull(message = "ID do produto é obrigatório.")
        UUID produtoID,

        @NotNull(message = "ID da unidade é obrigatório.")
        UUID unidadeId,

        @Positive(message = "Quantidade é obrigatória e deve ser maior que zero.")
        @NotNull(message = "Quantidade não pode ser nula.")
        Integer quantidade,

        @NotBlank(message = "Tipo de movimentação é obrigatório.")
        MovimentacaoTipo tipo
) {
}
