package com.luc.raizesdeserto;

import com.luc.raizesdeserto.domain.entity.*;
import com.luc.raizesdeserto.domain.enums.CanalPedido;
import com.luc.raizesdeserto.domain.enums.FormaPagamento;
import com.luc.raizesdeserto.repository.PedidoRepository;
import com.luc.raizesdeserto.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    // 1. Mockando TODAS as dependências que o seu criarPedido usa!
    @Mock private PedidoRepository pedidoRepository;
    @Mock private UsuarioService usuarioService;
    @Mock private ProdutoService produtoService;
    @Mock private EstoqueService estoqueService;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    @DisplayName("Deve passar por todas as validações, salvar o pedido e retornar a entidade")
    void deveSalvarPedidoComSucesso() {

        UUID usuarioId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        // Criando Usuário Fake
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        // Criando Unidade Fake
        Unidade unidade = new Unidade();
        unidade.setId(unidadeId);

        // Criando Produto Fake
        Produto produto = new Produto();
        produto.setId(produtoId);
        produto.setPrecoBase(new BigDecimal("50.00"));

        // Criando Estoque Fake (Com quantidade suficiente)
        Estoque estoque = new Estoque();
        estoque.setProduto(produto);
        estoque.setQuantidadeAtual(10);

        // Criando Pagamento Fake
        Pagamento pagamento = new Pagamento();
        pagamento.setFormaPagamento(FormaPagamento.PIX);

        // Montando o ItemPedido Fake
        ItemPedido item = new ItemPedido();
        item.setProduto(produto);
        item.setQuantidade(2);

        // Montando o Pedido de Entrada
        Pedido pedidoEntrada = new Pedido();
        pedidoEntrada.setCanalPedido(CanalPedido.WEB);
        pedidoEntrada.setUsuario(usuario);
        pedidoEntrada.setUnidade(unidade);
        pedidoEntrada.setPagamento(pagamento);
        pedidoEntrada.setItens(new ArrayList<>(List.of(item)));

        item.setPedido(pedidoEntrada);

        // Montando o Pedido Retornado pelo Banco
        Pedido pedidoSalvoNoBanco = new Pedido();
        pedidoSalvoNoBanco.setId(UUID.randomUUID());
        pedidoSalvoNoBanco.setValorTotal(new BigDecimal("100.00")); // 2 * 50.00


        // Libera o Usuário
        when(usuarioService.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

        // Libera o Produto (Existe e está Ativo)
        when(produtoService.existeProduto(produtoId)).thenReturn(true);
        when(produtoService.estaAtivo(produtoId)).thenReturn(true);

        // Libera o Estoque
        when(estoqueService.consultarItemEspecifico(unidadeId, produtoId)).thenReturn(Optional.of(estoque));

        // Libera o Repository Final
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvoNoBanco);

        Pedido resultado = pedidoService.criarPedido(pedidoEntrada);

        assertNotNull(resultado.getId());
        assertEquals(new BigDecimal("100.00"), resultado.getValorTotal());

        // Confirma se as ferramentas externas foram chamadas corretamente
        verify(estoqueService, times(1)).debitar(produtoId, unidadeId, 2);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(auditoriaService, times(1)).registrarTransicao(any(), any(), any(), any(), anyString());
    }
}