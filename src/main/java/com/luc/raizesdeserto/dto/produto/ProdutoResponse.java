package com.luc.raizesdeserto.dto.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoResponse(
        UUID id,
        Boolean ativo,
        String nome,
        String descricao,
        BigDecimal precoBase,
        String categoria,
        Boolean sazonal
) {
}
