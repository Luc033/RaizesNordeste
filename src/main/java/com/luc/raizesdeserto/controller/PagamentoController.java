package com.luc.raizesdeserto.controller;

import com.luc.raizesdeserto.dto.pagamento.PagamentoMockRequest;
import com.luc.raizesdeserto.dto.pagamento.PagamentoResponse;
import com.luc.raizesdeserto.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping("mock/callback")
    public ResponseEntity<Void> processarRetornoPagamento(
            @Valid @RequestBody PagamentoMockRequest request
            ){
        pagamentoService.registrarRetorno(request.pedidoId(), request.statusPagamento(), request.payloadWebhook());
        return ResponseEntity.ok().build();
    }

}

