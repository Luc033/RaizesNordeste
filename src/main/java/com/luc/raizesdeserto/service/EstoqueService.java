package com.luc.raizesdeserto.service;

import com.luc.raizesdeserto.domain.entity.Estoque;
import com.luc.raizesdeserto.repository.EstoqueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;

    public EstoqueService(EstoqueRepository estoqueRepository) {
        this.estoqueRepository = estoqueRepository;
    }

    public List<Estoque> consultarPorUnidade(UUID unidadeId) {
        return estoqueRepository.findAllByUnidadeId(unidadeId);
    }

    public Estoque consultarItemEspecifico(UUID unidadeId, UUID produtoId) {
        return estoqueRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado no estoque desta unidade."));
    }

    // TODO: debitar() e creditar()
}
