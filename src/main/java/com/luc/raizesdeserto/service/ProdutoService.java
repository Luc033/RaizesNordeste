package com.luc.raizesdeserto.service;

import com.luc.raizesdeserto.domain.entity.Produto;
import com.luc.raizesdeserto.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;


    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public void salvar(Produto produto){
        this.produtoRepository.save(produto);
    }

    public Optional<Produto> buscarPorId(UUID id){
        return this.produtoRepository.findById(id);
    }

    public List<Produto> listar(){
        return this.produtoRepository.findAll();
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
