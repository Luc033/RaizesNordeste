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

/**
 * Orquestra as regras de negócio relacionadas ao estoque por unidade,
 * incluindo consulta, débito e crédito de produtos disponíveis.
 */
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

    /**
     * Consulta um item de estoque pertencente a uma unidade específica, garantindo que o
     * produto esteja cadastrado no estoque da unidade informada. Não permite prosseguir com
     * operações sobre produtos inexistentes na unidade.
     *
     * @param unidadeId unidade na qual o estoque será consultado
     * @param produtoId produto cuja disponibilidade será validada
     * @return item de estoque correspondente à unidade e ao produto informados
     * @throws EntityNotFoundException quando o produto não está vinculado ao estoque da unidade
     */
    public Optional<Estoque> consultarItemEspecifico(UUID unidadeId, UUID produtoId) {
        return Optional.of(estoqueRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado no estoque desta unidade.")));
    }

    /**
     * Debita a quantidade informada do estoque de um produto em uma unidade específica,
     * garantindo a integridade do saldo disponível. Não permite movimentações com quantidade
     * inválida, produtos inexistentes no estoque da unidade ou operações que resultem em saldo negativo.
     *
     * @param produtoId produto que terá o estoque debitado
     * @param unidadeId unidade onde a movimentação será realizada
     * @param quantidade quantidade a ser debitada do estoque
     * @throws IllegalArgumentException quando a quantidade informada é menor ou igual a zero
     * @throws EntityNotFoundException quando não existe registro de estoque para o produto na unidade informada
     * @throws ArithmeticException quando a quantidade solicitada excede o saldo disponível em estoque
     */
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

    /**
     * Credita uma quantidade no estoque de um produto em uma unidade específica,
     * reutilizando o registro existente ou criando um novo vínculo de estoque quando
     * ainda não houver saldo cadastrado para aquela combinação de produto e unidade.
     * Não permite créditos com quantidade inválida nem criação de estoque para produto
     * ou unidade inexistentes.
     *
     * @param produtoId produto que terá o estoque creditado
     * @param unidadeId unidade onde o crédito será aplicado
     * @param quantidade quantidade a ser adicionada ao saldo atual ou usada como saldo inicial
     * @throws IllegalArgumentException quando a quantidade informada é menor ou igual a zero
     * @throws EntityNotFoundException quando o estoque precisa ser criado, mas o produto ou a unidade não existem
     */
    @Transactional
    public void creditar(UUID produtoId, UUID unidadeId, int quantidade) {
        // validação básica de entrada
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade a creditar deve ser maior que zero.");
        }

        // tenta buscar o estoque
        Optional<Estoque> estoqueOptional = estoqueRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId);

        Estoque estoque;

        if (estoqueOptional.isPresent()) {
            // CENÁRIO A: O registro de estoque já existe
            estoque = estoqueOptional.get();

            // apenas soma o valor ao saldo atual
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
