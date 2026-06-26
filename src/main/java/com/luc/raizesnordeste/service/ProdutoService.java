package com.luc.raizesnordeste.service;

import com.luc.raizesnordeste.domain.entity.Produto;
import com.luc.raizesnordeste.dto.produto.ProdutoRequest;
import com.luc.raizesnordeste.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Gerencia o catálogo de produtos da aplicação, centralizando operações
 * de cadastro, atualização, exclusão, consulta e validação de disponibilidade.
 */
@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final AuditoriaService auditoriaService;

    public ProdutoService(ProdutoRepository produtoRepository, AuditoriaService auditoriaService) {
        this.produtoRepository = produtoRepository;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Salva ou atualiza um produto na base de dados.
     *
     * <p>Este método pertence à camada de Service e centraliza a persistência de produtos,
     * delegando ao repositório a gravação da entidade informada.</p>
     *
     * @param produto produto que será salvo ou atualizado.
     * @return produto salvo com os dados persistidos.
     */
    @Transactional
    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }


    /**
     * Atualiza os dados cadastrais de um produto existente.
     *
     * <p>Este método pertence à camada de Service e aplica a regra de atualização de produto,
     * buscando o registro pelo identificador informado e substituindo seus dados pelos valores
     * recebidos na requisição.</p>
     *
     * @param id identificador único do produto que será atualizado.
     * @param request dados atualizados do produto, como nome, descrição, preço base, categoria e sazonalidade.
     * @return produto atualizado e salvo na base de dados.
     * @throws EntityNotFoundException quando nenhum produto é encontrado para o identificador informado.
     */
    @Transactional
    public Produto atualizar(UUID id, ProdutoRequest request) {
        Produto produtoExistente = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com o ID: " + id));

        produtoExistente.setNome(request.nome().trim());
        produtoExistente.setDescricao(request.descricao());
        produtoExistente.setPrecoBase(request.precoBase());
        produtoExistente.setCategoria(request.categoria());
        produtoExistente.setSazonal(request.sazonal());

        return produtoRepository.save(produtoExistente);
    }

    /**
     * Remove um produto existente da base de dados.
     *
     * <p>Este método pertence à camada de Service e aplica a regra de exclusão de produto,
     * buscando o registro pelo identificador informado antes de executar a remoção.</p>
     *
     * @param id identificador único do produto que será removido.
     * @throws EntityNotFoundException quando nenhum produto é encontrado para o identificador informado.
     */
    @Transactional
    public void deletar(UUID id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com o ID: " + id));

        produtoRepository.delete(produto);
    }

    /**
     * Busca um produto pelo seu identificador único.
     *
     * <p>Este método pertence à camada de Service e centraliza a recuperação de um produto,
     * lançando exceção caso o registro não exista na base de dados.</p>
     *
     * @param id identificador único do produto.
     * @return produto encontrado.
     * @throws EntityNotFoundException quando nenhum produto é encontrado para o identificador informado.
     */
    public Produto buscarPorId(UUID id){
        return this.produtoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));
    }

    /**
     * Lista todos os produtos cadastrados na base de dados.
     *
     * <p>Este método pertence à camada de Service e centraliza a consulta geral de produtos,
     * retornando todos os registros disponíveis sem aplicação de filtros.</p>
     *
     * @return lista com todos os produtos cadastrados.
     */
    public List<Produto> listar(){
        return this.produtoRepository.findAll();
    }

    /**
     * Lista os produtos vinculados a uma unidade específica.
     *
     * <p>Este método pertence à camada de Service e centraliza a consulta de produtos
     * disponíveis em uma determinada unidade, utilizando o identificador informado como filtro.</p>
     *
     * @param unidadeId identificador único da unidade utilizada para filtrar os produtos.
     * @return lista de produtos associados à unidade informada.
     */
    public List<Produto> listarProdutosPorUnidade(UUID unidadeId){
        return this.produtoRepository.findAllByUnidadeId(unidadeId);
    }

    /**
     * Verifica se existe um produto cadastrado com o identificador informado.
     *
     * <p>Este método pertence à camada de Service e apoia validações de negócio
     * que dependem da existência prévia do produto na base de dados.</p>
     *
     * @param id identificador único do produto que será verificado.
     * @return {@code true} se o produto existir; caso contrário, {@code false}.
     */
    public Boolean existeProduto(UUID id){
        return this.produtoRepository.existsById(id);
    }


    /**
     * Verifica se um produto está ativo na base de dados.
     *
     * <p>Este método pertence à camada de Service e apoia validações de negócio
     * que impedem o uso de produtos inativos em operações como criação de pedidos.</p>
     *
     * @param id identificador único do produto que será verificado.
     * @return {@code true} se o produto existir e estiver ativo; caso contrário, {@code false}.
     */
    public Boolean estaAtivo(UUID id){
        var produto = produtoRepository.findProdutoByIdAndAtivoTrue(id);

        if(produto.isPresent()){
            return true;
        }else{
            return false;
        }
    }

}
