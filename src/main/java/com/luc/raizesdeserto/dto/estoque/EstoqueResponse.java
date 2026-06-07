package com.luc.raizesdeserto.dto.estoque;

import com.luc.raizesdeserto.domain.entity.Estoque;

import java.util.UUID;

public record EstoqueResponse(
        UUID unidadeId,
        UUID produtoId,
        Integer quantidadeAtual
) {
    public EstoqueResponse(Estoque e){
        this(e.getId(), e.getProduto().getId(), e.getQuantidadeAtual());
    }
}
