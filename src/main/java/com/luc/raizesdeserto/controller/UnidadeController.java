package com.luc.raizesdeserto.controller;

import com.luc.raizesdeserto.dto.unidade.UnidadeResponse;
import com.luc.raizesdeserto.service.UnidadeService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController()
@RequestMapping("/unidades")
public class UnidadeController {

    private final UnidadeService unidadeService;

    public UnidadeController(UnidadeService unidadeService) {
        this.unidadeService = unidadeService;
    }

    @GetMapping()
    public ResponseEntity<List<UnidadeResponse>> listarTodasUnidades() {
        var unidades = unidadeService.listar().stream().map(u -> new UnidadeResponse(u)).toList();
        return ResponseEntity.ok().body(unidades);
    }

    @GetMapping("{unidadeId}")
    public ResponseEntity<UnidadeResponse> buscarUnidade(@PathVariable UUID unidadeId) {
        return ResponseEntity.ok().body(new UnidadeResponse(unidadeService.buscarPorId(unidadeId)));
    }

}
