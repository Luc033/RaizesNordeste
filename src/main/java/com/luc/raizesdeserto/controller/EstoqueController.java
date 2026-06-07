package com.luc.raizesdeserto.controller;

import com.luc.raizesdeserto.domain.enums.MovimentacaoTipo;
import com.luc.raizesdeserto.dto.estoque.EstoqueResponse;
import com.luc.raizesdeserto.dto.estoque.MovimentacaoRequest;
import com.luc.raizesdeserto.service.EstoqueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    // TODO:  GET   /estoque?unidadeId=
    //        POST /estoque/movimentacao

    @GetMapping()
    public ResponseEntity<List<EstoqueResponse>> listarEstoquePorUnidade(@RequestParam UUID unidadeId) {
        var estoque = estoqueService.consultarPorUnidade(unidadeId).stream().map(e -> new EstoqueResponse(e)).toList();
        return ResponseEntity.ok().body(estoque);
    }

    @PostMapping("movimentacao")
    public ResponseEntity lancarMovimentacao(@RequestBody MovimentacaoRequest request){
        switch (request.tipo()){
            case MovimentacaoTipo.ENTRADA:
                estoqueService.creditar(request.produtoID(), request.unidadeId(), request.quantidade());
                return ResponseEntity.ok().build();
            case MovimentacaoTipo.SAIDA:
                estoqueService.debitar(request.produtoID(), request.unidadeId(), request.quantidade());
                return ResponseEntity.ok().build();
            default:
                return ResponseEntity.badRequest().build();
        }

    }

}
