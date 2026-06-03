package com.luc.raizesdeserto.service;

import com.luc.raizesdeserto.domain.entity.Pedido;
import com.luc.raizesdeserto.domain.enums.CanalPedido;
import com.luc.raizesdeserto.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;
    private final EstoqueService estoqueService;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoService produtoService, EstoqueService estoqueService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoService = produtoService;
        this.estoqueService = estoqueService;
    }

    // criar() valida estoque, canal, itens;
    // atualizarStatus()

    public void criarPedido(Pedido pedido) {
        // Verifica se o canal fornecido no parâmetro existe no enum CanalPedido
        if(Arrays.stream(CanalPedido.values()).anyMatch(c -> c.equals(pedido.getCanalPedido()))){

            // Verifica: se a lista de itens está vazia;
            // e integridade do preço
            if(pedido.getItens().isEmpty()){
                throw new IllegalArgumentException("Lista de itens está vazia.");
            }

            pedido.getItens().stream().forEach(item -> {
                // Verifica a existência dos itens no Banco
                if(!produtoService.existeProduto(item.getId())){
                    throw new EntityNotFoundException("Item não encontrado.");
            }
                // Verifica se o produto está ativo
                if(!produtoService.estaAtivo(item.getId())){
                    throw new IllegalArgumentException("Produto inativo.");
                }

                // Verifica se o item possui estoque disponível
                var itemEstoque = estoqueService.consultarItemEspecifico(item.getPedido().getUnidade().getId(), item.getProduto().getId());
                if(itemEstoque.isPresent()){
                    // o produto existe nessa unidade, necessário validar estoque agora
                    if(true){
                        // estoque ok, pode criar pedido com o preço do produto no banco (aqui entra a integridade de preço)
                    }else{
                        // retornar erro, estoque insuficiente
                    }
                }else{
                    // Retorna erro pois o produto id ou unidade id não existe
                }
            });
        }else{
            // Retorna que o canal do pedido é inválido - 400 Bad Request
        }

        
    }




}
