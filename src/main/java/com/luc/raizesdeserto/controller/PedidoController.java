package com.luc.raizesdeserto.controller;

import com.luc.raizesdeserto.config.JWTUserData;
import com.luc.raizesdeserto.domain.entity.*;
import com.luc.raizesdeserto.domain.enums.CanalPedido;
import com.luc.raizesdeserto.domain.enums.FormaPagamento;
import com.luc.raizesdeserto.dto.pedido.AtualizarStatusRequest;
import com.luc.raizesdeserto.dto.pedido.CriarPedidoRequest;
import com.luc.raizesdeserto.dto.pedido.PedidoPagamentoResponse;
import com.luc.raizesdeserto.dto.pedido.PedidoResponse;
import com.luc.raizesdeserto.infra.GatewayPagamentoClient;
import com.luc.raizesdeserto.service.PagamentoService;
import com.luc.raizesdeserto.service.PedidoService;
import com.luc.raizesdeserto.service.UnidadeService;
import com.luc.raizesdeserto.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("pedidos")
public class PedidoController {
    private final PedidoService pedidoService;
    private final UnidadeService unidadeService;
    private final UsuarioService usuarioService;
    private final GatewayPagamentoClient pagamentoClient;
    private final PagamentoService pagamentoService;


    public PedidoController(PedidoService pedidoService, UnidadeService unidadeService, UsuarioService usuarioService, GatewayPagamentoClient pagamentoClient, PagamentoService pagamentoService) {
        this.pedidoService = pedidoService;
        this.unidadeService = unidadeService;
        this.usuarioService = usuarioService;
        this.pagamentoClient = pagamentoClient;
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_ATENDENTE')")
    public ResponseEntity<PedidoPagamentoResponse> criarPedido(@Valid @RequestBody CriarPedidoRequest request,
                                                               @AuthenticationPrincipal JWTUserData usuario){
        Pagamento pagamento = new Pagamento();
        pagamento.setFormaPagamento(request.formaPagamento());
        Pedido pedido = new Pedido();
        List<ItemPedido> itens = new ArrayList<>(request.itens().stream()
                .map(i -> new ItemPedido(new Produto(i.produtoId()), i.quantidade())).toList());
        pedido.setUnidade(unidadeService.buscarPorId(request.unidadeId()));
        pedido.setCanalPedido(request.canalPedido());
        pedido.adicionarItens(itens);
        pedido.setUsuario(usuarioService.buscarPorId(usuario.id()).get());
        pedido.setPagamento(pagamento);


        // pedido criado
        var pedidoSalvo = pedidoService.criarPedido(pedido);

        // envia o pedido para o gateway de pagamento e o mesmo retorna o link de pagamento
        var response = pagamentoClient.enviarSolicitacaoPagamentoMock(pedidoSalvo);

        // retorna ao cliente o pedido criado junto do link de pagamento
        return ResponseEntity.ok().body(new PedidoPagamentoResponse(pedidoSalvo, response)) ;


    }

    @GetMapping("{id}")
    public Pedido buscarPedido(@PathVariable UUID id){
        return pedidoService.buscarPorId(id);
    }

    @PatchMapping("{id}/status")
    public ResponseEntity<PedidoResponse> atualizarStatus(@PathVariable UUID id,
                                                          @Valid @RequestBody AtualizarStatusRequest request,
                                                          @AuthenticationPrincipal Usuario usuario){
        var pedidoAtualizado = pedidoService.atualizarStatus(usuario, id, request.status(), request.observacao());
        return ResponseEntity.ok().body(new PedidoResponse(pedidoAtualizado));

    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorCanal(@RequestParam CanalPedido canalPedido){
        var pedidos = pedidoService.listarPedidosPorCanal(canalPedido);
        return ResponseEntity.ok().body(pedidos.stream().map(p -> new PedidoResponse(p)).toList());
    }


}
