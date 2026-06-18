package com.luc.raizesdeserto;

import com.luc.raizesdeserto.domain.entity.*;
import com.luc.raizesdeserto.domain.enums.CanalPedido;
import com.luc.raizesdeserto.domain.enums.FormaPagamento;
import com.luc.raizesdeserto.domain.enums.Status;
import com.luc.raizesdeserto.repository.PedidoRepository;
import com.luc.raizesdeserto.service.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService - Regras de negócio de criação e atualização de pedidos")
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ProdutoService produtoService;
    @Mock
    private EstoqueService estoqueService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private PedidoService pedidoService;

    private Usuario usuario;
    private Unidade unidade;
    private Produto produto;
    private Estoque estoque;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        unidade = new Unidade();
        unidade.setId(UUID.randomUUID());

        produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setNome("Pastel de Carne");
        produto.setPrecoBase(new BigDecimal("15.00"));

        estoque = new Estoque();
        estoque.setProduto(produto);
        estoque.setUnidade(unidade);
        estoque.setQuantidadeAtual(10);
    }

    private Pedido montarPedidoValido(int quantidade) {
        Pedido pedido = new Pedido();
        pedido.setCanalPedido(CanalPedido.APP);
        pedido.setUnidade(unidade);
        pedido.setUsuario(usuario);

        Pagamento pagamento = new Pagamento();
        pagamento.setFormaPagamento(FormaPagamento.PIX);
        pedido.setPagamento(pagamento);

        ItemPedido item = new ItemPedido(produto, quantidade);
        pedido.adicionarItens(List.of(item));
        return pedido;
    }

    @Test
    @DisplayName("Deve criar pedido com sucesso quando estoque é suficiente e usuário existe")
    void deveCriarPedidoComSucesso_QuandoEstoqueSuficiente() {
        // Arrange
        Pedido novoPedido = montarPedidoValido(2);

        when(usuarioService.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));
        when(produtoService.existeProduto(produto.getId())).thenReturn(true);
        when(produtoService.estaAtivo(produto.getId())).thenReturn(true);
        when(estoqueService.consultarItemEspecifico(unidade.getId(), produto.getId()))
                .thenReturn(Optional.of(estoque));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pedido resultado = pedidoService.criarPedido(novoPedido);

        // Assert
        assertNotNull(resultado);
        assertEquals(Status.AGUARDANDO_PAGAMENTO, resultado.getStatus());
        assertEquals(new BigDecimal("30.00"), resultado.getValorTotal());
        verify(estoqueService, times(1)).debitar(produto.getId(), unidade.getId(), 2);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(auditoriaService, times(1))
                .registrarTransicao(any(Pedido.class), eq(usuario), isNull(), eq(Status.AGUARDANDO_PAGAMENTO), anyString());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando estoque é insuficiente")
    void deveLancarExcecao_QuandoEstoqueInsuficiente() {
        // Arrange
        Pedido novoPedido = montarPedidoValido(50); // quantidade maior que o estoque (10)

        when(usuarioService.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));
        when(produtoService.existeProduto(produto.getId())).thenReturn(true);
        when(produtoService.estaAtivo(produto.getId())).thenReturn(true);
        when(estoqueService.consultarItemEspecifico(unidade.getId(), produto.getId()))
                .thenReturn(Optional.of(estoque));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.criarPedido(novoPedido));

        assertTrue(ex.getMessage().contains("Estoque insuficiente"));
        verify(estoqueService, never()).debitar(any(), any(), anyInt());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando produto está inativo")
    void deveLancarExcecao_QuandoProdutoInativo() {
        // Arrange
        Pedido novoPedido = montarPedidoValido(1);

        when(usuarioService.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));
        when(produtoService.existeProduto(produto.getId())).thenReturn(true);
        when(produtoService.estaAtivo(produto.getId())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.criarPedido(novoPedido));

        assertTrue(ex.getMessage().contains("Produto inativo"));
        verify(estoqueService, never()).consultarItemEspecifico(any(), any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando produto não existe no banco")
    void deveLancarExcecao_QuandoProdutoNaoExiste() {
        // Arrange
        Pedido novoPedido = montarPedidoValido(1);

        when(usuarioService.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));
        when(produtoService.existeProduto(produto.getId())).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> pedidoService.criarPedido(novoPedido));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando lista de itens está vazia")
    void deveLancarExcecao_QuandoListaItensVazia() {
        // Arrange
        Pedido novoPedido = new Pedido();
        novoPedido.setCanalPedido(CanalPedido.APP);
        novoPedido.setUnidade(unidade);
        novoPedido.setUsuario(usuario);
        novoPedido.setPagamento(new Pagamento());
        // itens vazios por padrão (ArrayList vazia)

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.criarPedido(novoPedido));

        assertTrue(ex.getMessage().contains("Lista de itens está vazia"));
        verify(usuarioService, never()).buscarPorId(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando canal do pedido é inválido")
    void deveLancarExcecao_QuandoCanalInvalido() {
        // Arrange
        Pedido novoPedido = montarPedidoValido(1);
        novoPedido.setCanalPedido(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> pedidoService.criarPedido(novoPedido));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando usuário não existe")
    void deveLancarExcecao_QuandoUsuarioNaoExiste() {
        // Arrange
        Pedido novoPedido = montarPedidoValido(1);

        when(usuarioService.buscarPorId(usuario.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> pedidoService.criarPedido(novoPedido));
        verify(produtoService, never()).existeProduto(any());
    }

    @Test
    @DisplayName("Deve atualizar status do pedido com sucesso e registrar auditoria")
    void deveAtualizarStatusComSucesso() {
        // Arrange
        UUID pedidoId = UUID.randomUUID();
        Pedido pedidoExistente = new Pedido();
        pedidoExistente.setId(pedidoId);
        pedidoExistente.setStatus(Status.EM_PREPARO);

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedidoExistente));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Pedido resultado = pedidoService.atualizarStatus(usuario, pedidoId, Status.PRONTO, "Pedido finalizado pela cozinha.");

        // Assert
        assertEquals(Status.PRONTO, resultado.getStatus());
        verify(auditoriaService, times(1))
                .registrarTransicao(pedidoExistente, usuario, Status.EM_PREPARO, Status.PRONTO, "Pedido finalizado pela cozinha.");
        verify(pedidoRepository, times(1)).save(pedidoExistente);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao atualizar status de pedido inexistente")
    void deveLancarExcecao_QuandoAtualizarStatusDePedidoInexistente() {
        // Arrange
        UUID pedidoId = UUID.randomUUID();
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> pedidoService.atualizarStatus(usuario, pedidoId, Status.PRONTO, "obs"));

        verify(auditoriaService, never()).registrarTransicao(any(), any(), any(), any(), any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar pedido ao buscar por ID existente")
    void deveRetornarPedido_QuandoBuscarPorIdExistente() {
        // Arrange
        UUID pedidoId = UUID.randomUUID();
        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // Act
        Pedido resultado = pedidoService.buscarPorId(pedidoId);

        // Assert
        assertEquals(pedidoId, resultado.getId());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao buscar pedido por ID inexistente")
    void deveLancarExcecao_QuandoBuscarPedidoPorIdInexistente() {
        // Arrange
        UUID pedidoId = UUID.randomUUID();
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> pedidoService.buscarPorId(pedidoId));
    }
}