package com.luc.raizesnordeste.service;

import com.luc.raizesnordeste.domain.entity.Estoque;
import com.luc.raizesnordeste.domain.entity.ItemPedido;
import com.luc.raizesnordeste.domain.entity.Pagamento;
import com.luc.raizesnordeste.domain.entity.Pedido;
import com.luc.raizesnordeste.domain.enums.Status;
import com.luc.raizesnordeste.domain.enums.StatusPagamento;
import com.luc.raizesnordeste.dto.pagamento.PagamentoMockRequest;
import com.luc.raizesnordeste.repository.PagamentoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Orquestra o fluxo de pagamento dos pedidos, incluindo solicitação mock,
 * registro de retorno do gateway e tratamento de timeout com estorno de estoque.
 */
@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoService pedidoService;
    private final EstoqueService estoqueService;
    private final AuditoriaService auditoriaService;

    public PagamentoService(PagamentoRepository pagamentoRepository, PedidoService pedidoService, EstoqueService estoqueService, AuditoriaService auditoriaService) {
        this.pagamentoRepository = pagamentoRepository;
        this.pedidoService = pedidoService;
        this.estoqueService = estoqueService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Solicita um pagamento utilizando o gateway de pagamento mock para um pedido.
     * Valida se o pedido está aguardando pagamento, registra os dados da solicitação
     * e retorna um link fictício para simulação do fluxo de pagamento.
     *
     * @param pedido Pedido para o qual será gerada a solicitação de pagamento.
     * @return Link fictício utilizado para simular o redirecionamento ao gateway de pagamento.
     * @throws IllegalArgumentException Se o pedido não estiver com o status
     *                                  {@code AGUARDANDO_PAGAMENTO}.
     */
    @Transactional
    public String solicitarMock(Pedido pedido) {
        // Valida se o status do pedido está na situação própria para realizar o pagamento
        if (!pedido.getStatus().equals(Status.AGUARDANDO_PAGAMENTO)) {
            throw new IllegalArgumentException("Pedido não está apto para realizar o pagamento. Status atual: " +
                    pedido.getStatus() + "");
        }

        Pagamento pagamento = pedido.getPagamento();

        String linkFalso = "https://pagamento-mock.com.br/pay/" + pedido.getId();

        pagamento.setGatewayPagamento("MOCK_GATEWAY");
        pagamento.setSolicitadoEm(LocalDateTime.now());
        pagamentoRepository.save(pagamento);

        return linkFalso;
    }


    /**
     * Registra o retorno do gateway de pagamento mock para um pedido.
     * Valida se o pagamento está pendente e atualiza o status do pagamento e do
     * pedido conforme o resultado informado pelo gateway.
     *
     * @param request Dados retornados pelo gateway de pagamento mock.
     * @throws IllegalArgumentException Se o pagamento do pedido não estiver com o
     *                                  status {@code PENDENTE}.
     */
    @Transactional
    public void registrarRetorno(PagamentoMockRequest request) {
        var pedidoId = request.pedidoId();
        var statusPagamento = request.statusPagamento();
        var pedido = pedidoService.buscarPorId(pedidoId);

        if(!pedido.getPagamento().getStatusPagamento().equals(StatusPagamento.PENDENTE)){
            throw new IllegalArgumentException("Não é possível registrar o retorno do pagamento. Status atual: " + pedido.getPagamento().getStatusPagamento() + "");
        }

        if(statusPagamento.equals(StatusPagamento.APROVADO)){
            pedido.getPagamento().setConfirmadoEm(LocalDateTime.now());
            pedido.getPagamento().setPayloadRetorno(request.toString());
            pedido.setStatus(Status.EM_PREPARO);
            pedidoService.atualizarStatus(null, pedidoId, Status.EM_PREPARO, "Pedido aprovado pelo gateway.");
            pedido.getPagamento().setStatusPagamento(StatusPagamento.APROVADO);
        }else if(statusPagamento.equals(StatusPagamento.RECUSADO)){
            pedido.getPagamento().setConfirmadoEm(LocalDateTime.now());
            pedido.getPagamento().setPayloadRetorno(request.toString());
        }
    }

    /**
     * Processa periodicamente, a cada 10 segundos, os pedidos que excederam o tempo
     * limite de 10 minutos para pagamento. Cancela os pedidos pendentes, atualiza o
     * status do pagamento para {@code TIMEOUT} e realiza a devolução dos itens ao estoque.
     */
    @Transactional
    @Scheduled(fixedDelay = 10000)
    public void tratarTimeout(){
        var pedidos = pedidoService.listarPedidosAguardandoPagamento();

        if(!pedidos.isEmpty()){
            for(Pedido pedido : pedidos){
                pedidoService.atualizarStatus(null, pedido.getId(), Status.CANCELADO, "Pedido cancelado por inatividade.");
                Pagamento pagamento = pedido.getPagamento();
                pagamento.setStatusPagamento(StatusPagamento.TIMEOUT);
                pagamentoRepository.save(pagamento);
                for(ItemPedido item : pedido.getItens()) {
                    Optional<Estoque> estoque = estoqueService.consultarItemEspecifico(pedido.getUnidade().getId(), item.getProduto().getId());
                    if(estoque.isPresent()){
                        estoqueService.creditar(item.getProduto().getId(), pedido.getUnidade().getId(), item.getQuantidade());
                    }
                }

            }
        }
    }


}
