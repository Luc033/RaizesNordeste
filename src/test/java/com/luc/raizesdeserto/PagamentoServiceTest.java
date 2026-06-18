package com.luc.raizesdeserto;

import com.luc.raizesdeserto.domain.entity.*;
import com.luc.raizesdeserto.domain.enums.FormaPagamento;
import com.luc.raizesdeserto.domain.enums.Status;
import com.luc.raizesdeserto.domain.enums.StatusPagamento;
import com.luc.raizesdeserto.dto.pagamento.PagamentoMockRequest;
import com.luc.raizesdeserto.repository.PagamentoRepository;
import com.luc.raizesdeserto.service.AuditoriaService;
import com.luc.raizesdeserto.service.EstoqueService;
import com.luc.raizesdeserto.service.PagamentoService;
import com.luc.raizesdeserto.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagamentoService - Regras de webhook de pagamento e timeout")
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;
    @Mock
    private PedidoService pedidoService;
    @Mock
    private EstoqueService estoqueService;
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private PagamentoService pagamentoService;

    private Pedido pedido;
    private Pagamento pagamento;
    private UUID pedidoId;

    @BeforeEach
    void setUp() {
        pedidoId = UUID.randomUUID();
        pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setStatus(Status.AGUARDANDO_PAGAMENTO);

        pagamento = new Pagamento();
        pagamento.setFormaPagamento(FormaPagamento.PIX);
        pagamento.setStatusPagamento(StatusPagamento.PENDENTE);
        pedido.setPagamento(pagamento);
    }

    @Test
    @DisplayName("Deve gerar link mock de pagamento quando pedido está aguardando pagamento")
    void deveGerarLinkMock_QuandoPedidoAguardandoPagamento() {
        // Arrange
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamento);

        // Act
        String link = pagamentoService.solicitarMock(pedido);

        // Assert
        assertNotNull(link);
        assertTrue(link.contains(pedido.getId().toString()));
        assertEquals("MOCK_GATEWAY", pagamento.getGatewayPagamento());
        verify(pagamentoRepository, times(1)).save(pagamento);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao solicitar mock para pedido com status diferente de AGUARDANDO_PAGAMENTO")
    void deveLancarExcecao_QuandoSolicitarMockComStatusInvalido() {
        // Arrange
        pedido.setStatus(Status.EM_PREPARO);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pagamentoService.solicitarMock(pedido));

        assertTrue(ex.getMessage().contains("não está apto"));
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve avançar pedido para EM_PREPARO quando webhook retorna pagamento APROVADO")
    void deveAvancarParaEmPreparo_QuandoWebhookAprovado() {
        // Arrange
        PagamentoMockRequest request = new PagamentoMockRequest(pedidoId, StatusPagamento.APROVADO, LocalDateTime.now());

        when(pedidoService.buscarPorId(pedidoId)).thenReturn(pedido);

        // Act
        pagamentoService.registrarRetorno(request);

        // Assert
        assertEquals(StatusPagamento.APROVADO, pedido.getPagamento().getStatusPagamento());
        assertNotNull(pedido.getPagamento().getConfirmadoEm());
        verify(pedidoService, times(1))
                .atualizarStatus(isNull(), eq(pedidoId), eq(Status.EM_PREPARO), anyString());
    }


    @Test
    @DisplayName("Não deve alterar o status do pedido quando o pagamento for recusado")
    void naoDeveAlterarStatusPedidoQuandoPagamentoRecusado() {
        // Arrange
        var request = new PagamentoMockRequest(
                pedidoId,
                StatusPagamento.RECUSADO,
                LocalDateTime.now()
        );

        when(pedidoService.buscarPorId(pedidoId))
                .thenReturn(pedido);

        // Act
        pagamentoService.registrarRetorno(request);

        // Assert
        assertNotNull(
                pedido.getPagamento().getConfirmadoEm(),
                "A data de confirmação do pagamento deve ser preenchida"
        );

        verify(pedidoService, never())
                .atualizarStatus(any(), any(), any(), any());
    }
    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando pagamento do pedido não está mais PENDENTE")
    void deveLancarExcecao_QuandoPagamentoNaoEstaPendente() {
        // Arrange
        pagamento.setStatusPagamento(StatusPagamento.APROVADO);
        PagamentoMockRequest request = new PagamentoMockRequest(pedidoId, StatusPagamento.APROVADO, LocalDateTime.now());

        when(pedidoService.buscarPorId(pedidoId)).thenReturn(pedido);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> pagamentoService.registrarRetorno(request));
        verify(pedidoService, never()).atualizarStatus(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve cancelar pedido e creditar estoque quando ocorre timeout de pagamento")
    void deveCancelarPedidoECreditarEstoque_QuandoTimeout() {
        // Arrange
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());

        Unidade unidade = new Unidade();
        unidade.setId(UUID.randomUUID());
        pedido.setUnidade(unidade);

        ItemPedido item = new ItemPedido(produto, 3);
        pedido.adicionarItens(List.of(item));

        Estoque estoqueExistente = new Estoque();
        estoqueExistente.setProduto(produto);
        estoqueExistente.setUnidade(unidade);
        estoqueExistente.setQuantidadeAtual(5);

        when(pedidoService.listarPedidosAguardandoPagamento()).thenReturn(List.of(pedido));
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamento);
        when(estoqueService.consultarItemEspecifico(unidade.getId(), produto.getId()))
                .thenReturn(Optional.of(estoqueExistente));

        // Act
        pagamentoService.tratarTimeout();

        // Assert
        verify(pedidoService, times(1))
                .atualizarStatus(isNull(), eq(pedidoId), eq(Status.CANCELADO), anyString());
        assertEquals(StatusPagamento.TIMEOUT, pagamento.getStatusPagamento());
        verify(estoqueService, times(1)).creditar(produto.getId(), unidade.getId(), 3);
    }

    @Test
    @DisplayName("Não deve processar nada quando não há pedidos aguardando pagamento em timeout")
    void naoDeveProcessarNada_QuandoNaoHaPedidosEmTimeout() {
        // Arrange
        when(pedidoService.listarPedidosAguardandoPagamento()).thenReturn(List.of());

        // Act
        pagamentoService.tratarTimeout();

        // Assert
        verify(pedidoService, never()).atualizarStatus(any(), any(), any(), any());
        verify(estoqueService, never()).creditar(any(), any(), anyInt());
    }
}