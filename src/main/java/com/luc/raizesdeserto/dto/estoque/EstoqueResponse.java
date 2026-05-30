package com.luc.raizesdeserto.dto.estoque;

import java.util.UUID;

public record EstoqueResponse(
        UUID unidadeId,
        UUID produtoId,
        Integer quantidadeAtual
) {
}
