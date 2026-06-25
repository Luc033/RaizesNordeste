package com.luc.raizesnordeste.dto.estoque;

import com.luc.raizesnordeste.domain.entity.Estoque;

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
