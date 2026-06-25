package com.luc.raizesnordeste.dto.produto;

import com.luc.raizesnordeste.domain.entity.Produto;

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
    public ProdutoResponse(Produto p){
        this(p.getId(), p.getAtivo(), p.getNome(), p.getDescricao(), p.getPrecoBase(), p.getCategoria(), p.getSazonal());
    }

}
