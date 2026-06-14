package com.luc.raizesdeserto.controller;

import com.luc.raizesdeserto.domain.entity.Produto;
import com.luc.raizesdeserto.domain.entity.Usuario;
import com.luc.raizesdeserto.dto.produto.ProdutoRequest;
import com.luc.raizesdeserto.dto.produto.ProdutoResponse;
import com.luc.raizesdeserto.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<List<ProdutoResponse>> listarTodosProdutos() {
        var produtos = produtoService.listar().stream().map(p -> new ProdutoResponse(p)).toList();
        return ResponseEntity.ok().body(produtos);
    }

    @GetMapping("/unidades/{unidadeId}/produtos")
    public ResponseEntity<List<ProdutoResponse>> listarTodosProdutosPorUnidade(@PathVariable UUID unidadeId) {
        var produtos = produtoService.listarProdutosPorUnidade(unidadeId).stream().map(p -> new ProdutoResponse(p)).toList();
        return ResponseEntity.ok().body(produtos);
    }

    @GetMapping("/produtos/{id}")
    public ResponseEntity<ProdutoResponse> buscarProduto(@PathVariable UUID id) {
        return ResponseEntity.ok().body(new ProdutoResponse(produtoService.buscarPorId(id)));
    }

    @PostMapping("/produtos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ProdutoResponse> criarProduto(
            @Valid @RequestBody ProdutoRequest request) {

        Produto novoProduto = new Produto();
        novoProduto.setNome(request.nome().trim());
        novoProduto.setDescricao(request.descricao());
        novoProduto.setPrecoBase(request.precoBase());
        novoProduto.setCategoria(request.categoria());
        novoProduto.setSazonal(request.sazonal());

        Produto produtoSalvo = produtoService.salvar(novoProduto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProdutoResponse(produtoSalvo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ProdutoResponse> atualizarProduto(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoRequest request) {

        Produto produtoAtualizado = produtoService.atualizar(id, request);
        return ResponseEntity.ok(new ProdutoResponse(produtoAtualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> deletarProduto(
            @PathVariable UUID id) {

        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}