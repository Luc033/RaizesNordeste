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
import com.luc.raizesdeserto.service.PedidoService;
import com.luc.raizesdeserto.service.UnidadeService;
import com.luc.raizesdeserto.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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


    public PedidoController(PedidoService pedidoService, UnidadeService unidadeService, UsuarioService usuarioService, GatewayPagamentoClient pagamentoClient) {
        this.pedidoService = pedidoService;
        this.unidadeService = unidadeService;
        this.usuarioService = usuarioService;
        this.pagamentoClient = pagamentoClient;
    }

    @PostMapping
    public ResponseEntity<PedidoPagamentoResponse> criarPedido(@Valid @RequestBody CriarPedidoRequest request,
                                                               @AuthenticationPrincipal JWTUserData usuario){
        Pagamento pagamento = new Pagamento();
        pagamento.setFormaPagamento(request.formaPagamento());
        Pedido pedido = new Pedido();
        List<ItemPedido> itens = new ArrayList<>(request.itens().stream().map(i -> new ItemPedido(new Produto(i.produtoId()), i.quantidade())).toList());
        pedido.setUnidade(unidadeService.buscarPorId(request.unidadeId()));
        pedido.setCanalPedido(request.canalPedido());
        pedido.adicionarItens(itens);
        pedido.setUsuario(usuarioService.buscarPorId(usuario.id()).get());
        pedido.setPagamento(pagamento);
        var pedidoSalvo = pedidoService.criarPedido(pedido);

        String infoPagamento = null;
        if(pedidoSalvo.getPagamento().getFormaPagamento().equals(FormaPagamento.DINHEIRO)){
            infoPagamento = "Pagamento será realizado no momento da entrega/retirada. Separe o valor exato ou informe a necessidade de troco.";
        }else{
            String payloadGateway = String.format(
                    "{\"pedidoId\": \"%s\", \"valorTotal\": %s}",
                    pedidoSalvo.getId(),
                    pedidoSalvo.getValorTotal()
            );

            //dispara a requisição para o gateway MOCK
            String respostaGateway = pagamentoClient.enviarSolicitacaoPagamentoMock(payloadGateway, false);

            switch (pedidoSalvo.getPagamento().getFormaPagamento()){
                case PIX:
                    infoPagamento = "Pix: " + respostaGateway;
            }
            System.out.printf("Integração de pagamento: resposta do Gateway: %s", respostaGateway);
        }

        // pensando em incluir links novos no mock.io para ele retornar a estrutura correta de pagamento para os tipos de pagamento criados.
        // então, da forma que será a forma de pagamento do pedido atual, vou fazer um switch case para enviar a solicição para o gateway e retornar os dados (em cada em case) de forma personalizada,
        // retornar somente os dados necessário para pagamento para a aquela forma de pagamento específica contempla

        return ResponseEntity.ok().body(new PedidoPagamentoResponse(pedidoSalvo, infoPagamento)) ;

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
