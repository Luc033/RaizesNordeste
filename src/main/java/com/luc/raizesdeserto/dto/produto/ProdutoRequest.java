package com.luc.raizesdeserto.dto.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "Nome de produto é obrigatório.")
        @Size(min = 1, max = 100, message = "Nome deve estar entre 1 e 100 caracteres.")
        String nome,

        String descricao,

        @NotNull(message = "Preço de produto base é obrigatório.")
        @PositiveOrZero(message = "Preço de produto base deve ser igual ou maior que zero.")
        BigDecimal precoBase,

        @Size(min = 1, max = 80, message = "Categoria de produto deve estar entre 1 e 80 caracteres.")
        String categoria,

        Boolean sazonal
) {
}
