package com.luc.raizesdeserto.controller;

import com.luc.raizesdeserto.dto.produto.ProdutoResponse;
import com.luc.raizesdeserto.service.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController()
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/produtos")
    public ResponseEntity<List<ProdutoResponse>> listarTodosProdutos(){
        var produtos = produtoService.listar().stream().map(p -> new ProdutoResponse(p)).toList();
        return ResponseEntity.ok().body(produtos);
    }
    @GetMapping("/unidades/{unidadeId}/produtos")
    public ResponseEntity<List<ProdutoResponse>> listarTodosProdutosPorUnidade(@PathVariable UUID unidadeId){
        var produtos = produtoService.listarProdutosPorUnidade(unidadeId).stream().map(p -> new ProdutoResponse(p)).toList();
        return ResponseEntity.ok().body(produtos);
    }

    @GetMapping("/produtos/{id}")
    public ResponseEntity<ProdutoResponse> buscarProduto(@PathVariable UUID id){
        return ResponseEntity.ok().body(new ProdutoResponse(produtoService.buscarPorId(id)));
    }


}
