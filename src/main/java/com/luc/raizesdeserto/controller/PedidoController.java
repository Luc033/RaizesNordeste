package com.luc.raizesdeserto.controller;

import com.luc.raizesdeserto.domain.entity.ItemPedido;
import com.luc.raizesdeserto.domain.entity.Pedido;
import com.luc.raizesdeserto.domain.entity.Produto;
import com.luc.raizesdeserto.domain.entity.Usuario;
import com.luc.raizesdeserto.domain.enums.CanalPedido;
import com.luc.raizesdeserto.dto.pedido.AtualizarStatusRequest;
import com.luc.raizesdeserto.dto.pedido.CriarPedidoRequest;
import com.luc.raizesdeserto.dto.pedido.PedidoResponse;
import com.luc.raizesdeserto.service.PedidoService;
import com.luc.raizesdeserto.service.UnidadeService;
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


    public PedidoController(PedidoService pedidoService, UnidadeService unidadeService) {
        this.pedidoService = pedidoService;
        this.unidadeService = unidadeService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criarPedido(@Valid @RequestBody CriarPedidoRequest request,
                                                      @AuthenticationPrincipal Usuario usuario){
        Pedido pedido = new Pedido();
        List<ItemPedido> itens = new ArrayList<>(request.itens().stream().map(i -> new ItemPedido(new Produto(i.produtoId()), i.quantidade())).toList());
        pedido.setUnidade(unidadeService.buscarPorId(request.unidadeId()));
        pedido.setCanalPedido(request.canalPedido());
        pedido.adicionarItens(itens);
        pedido.setUsuario(usuario);
        var pedidoSalvo = pedidoService.criarPedido(pedido);
        return ResponseEntity.ok().body(new PedidoResponse(pedidoSalvo)) ;

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
