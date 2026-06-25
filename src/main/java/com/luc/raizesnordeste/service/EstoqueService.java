package com.luc.raizesnordeste.service;

import com.luc.raizesnordeste.domain.entity.Estoque;
import com.luc.raizesnordeste.domain.entity.Produto;
import com.luc.raizesnordeste.domain.entity.Unidade;
import com.luc.raizesnordeste.repository.EstoqueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final UnidadeService unidadeService;
    private final ProdutoService produtoService;

    public EstoqueService(EstoqueRepository estoqueRepository, UnidadeService unidadeService, ProdutoService produtoService) {
        this.estoqueRepository = estoqueRepository;
        this.unidadeService = unidadeService;
        this.produtoService = produtoService;
    }

    public List<Estoque> consultarPorUnidade(UUID unidadeId) {
        return estoqueRepository.findAllByUnidadeId(unidadeId);
    }

    public Optional<Estoque> consultarItemEspecifico(UUID unidadeId, UUID produtoId) {
        return Optional.of(estoqueRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado no estoque desta unidade.")));
    }

    @Transactional
    public void debitar(UUID produtoId, UUID unidadeId, int quantidade) {
        // validação básica de entrada
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade a debitar deve ser maior que zero.");
        }

        // busca o estoque existente
        Estoque estoque = this.consultarItemEspecifico(unidadeId, produtoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Estoque não encontrado para o Produto %s e Unidade %s.", produtoId, unidadeId)
                ));

        // verifica se tem saldo suficiente
        if (estoque.getQuantidadeAtual() < quantidade) {
            throw new ArithmeticException(
                    String.format("Quantidade a debitar (%d) é maior do que o saldo atual em estoque (%d).",
                            quantidade, estoque.getQuantidadeAtual())
            );
        }

        // subtrai o valor e salva
        int novoSaldo = estoque.getQuantidadeAtual() - quantidade;
        estoque.setQuantidadeAtual(novoSaldo);

        estoqueRepository.save(estoque);
    }


    @Transactional
    public void creditar(UUID produtoId, UUID unidadeId, int quantidade) {
        // validação básica de entrada
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade a creditar deve ser maior que zero.");
        }

        // tenta buscar o estoque
        Optional<Estoque> estoqueOptional = this.consultarItemEspecifico(unidadeId, produtoId);

        Estoque estoque;

        if (estoqueOptional.isPresent()) {
            // CENÁRIO A: O registro de estoque já existe
            estoque = estoqueOptional.get();

            // apenas somamos o valor ao saldo atual
            int novoSaldo = estoque.getQuantidadeAtual() + quantidade;
            estoque.setQuantidadeAtual(novoSaldo);
        } else {
            // CENÁRIO B: O registro de estoque não existe na unidade

            // buscando as entidades para fazer o vínculo
            Produto produto = produtoService.buscarPorId(produtoId);
            Unidade unidade = unidadeService.buscarPorId(unidadeId);

            // valida se produto/unidade existem
            if (produto == null || unidade == null) {
                throw new EntityNotFoundException(String.format("Produto ID (%s) e/ou Unidade ID (%s) não encontrados.", produtoId, unidadeId));
            }

            estoque = new Estoque();
            estoque.setProduto(produto);
            estoque.setUnidade(unidade);
            estoque.setQuantidadeAtual(quantidade);
        }
        estoqueRepository.save(estoque);
    }
}
