package com.luc.raizesnordeste.service;

import com.luc.raizesnordeste.domain.entity.Produto;
import com.luc.raizesnordeste.dto.produto.ProdutoRequest;
import com.luc.raizesnordeste.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final AuditoriaService auditoriaService;

    public ProdutoService(ProdutoRepository produtoRepository, AuditoriaService auditoriaService) {
        this.produtoRepository = produtoRepository;
        this.auditoriaService = auditoriaService;
    }


    @Transactional
    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

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

    @Transactional
    public void deletar(UUID id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com o ID: " + id));

        produtoRepository.delete(produto);
    }

    public Produto buscarPorId(UUID id){
        return this.produtoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));
    }

    public List<Produto> listar(){
        return this.produtoRepository.findAll();
    }

    public List<Produto> listarProdutosPorUnidade(UUID unidadeId){
        return this.produtoRepository.findAllByUnidadeId(unidadeId);
    }


    public Boolean existeProduto(UUID id){
        return this.produtoRepository.existsById(id);
    }

    public Boolean estaAtivo(UUID id){
        var produto = produtoRepository.findProdutoByIdAndAtivoTrue(id);

        if(produto.isPresent()){
            return true;
        }else{
            return false;
        }
    }

}
