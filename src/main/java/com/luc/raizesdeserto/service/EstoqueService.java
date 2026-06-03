package com.luc.raizesdeserto.service;

import com.luc.raizesdeserto.domain.entity.Estoque;
import com.luc.raizesdeserto.domain.entity.Produto;
import com.luc.raizesdeserto.domain.entity.Unidade;
import com.luc.raizesdeserto.repository.EstoqueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

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

    public void debitar(UUID produtoId, UUID unidadeId, int quantidade) {
        Optional<Produto> produto = produtoService.buscarPorId(produtoId);
        Optional<Unidade> unidade = unidadeService.buscarPorId(unidadeId);

        if (produto.isPresent() && unidade.isPresent()) {
            Estoque estoque = new Estoque();
            estoque.setProduto(produto.get());
            estoque.setUnidade(unidade.get());

            // [[ O PRIMEIRO METODO PODE RETORNA NULL POIS UNIDADE OU PRODUTO PODE NÃO EXISTIR ]]
            // Se a quantidade atual for menor que a quantidade a ser debitada, retorna erro
            if (this.consultarItemEspecifico(unidadeId, produtoId).get().getQuantidadeAtual() >= quantidade) {
                estoque.setQuantidadeAtual(quantidade);
                estoqueRepository.save(estoque);
            } else {
                // retorna o erro de que a quantidade a debitar é maior do que o saldo atual em estoque
                throw new IllegalArgumentException("Quantidade a debitar é maior do que o saldo atual em estoque.");

            }
        } else {
            // retorna o erro informando que produtoId e/ou unidadeId são obrigatórios
            throw new EntityNotFoundException("Produto ID e/ou Unidade ID não encontrados.");
        }
    }

    public void creditar(UUID produtoId, UUID unidadeId, int quantidade) {
        Optional<Produto> produto = produtoService.buscarPorId(produtoId);
        Optional<Unidade> unidade = unidadeService.buscarPorId(unidadeId);

        if (produto.isPresent() && unidade.isPresent()) {
            Estoque estoque = new Estoque();
            estoque.setProduto(produto.get());
            estoque.setUnidade(unidade.get());

            // Valida se a quantidade a creditar for menor ou igual a zero
            if (this.consultarItemEspecifico(unidadeId, produtoId).get().getQuantidadeAtual() > 0) {
                estoque.setQuantidadeAtual(quantidade);
                estoqueRepository.save(estoque);
            } else {
                // retorna o erro de que a quantidade a creditar é inválida
                throw new IllegalArgumentException("Quantidade a creditar não pode ser menor ou igual a zero.");

            }
        } else {
            // retorna o erro informando que produtoId e/ou unidadeId são obrigatórios
            throw new EntityNotFoundException("Produto ID e/ou Unidade ID não encontrados.");
        }
    }
}
