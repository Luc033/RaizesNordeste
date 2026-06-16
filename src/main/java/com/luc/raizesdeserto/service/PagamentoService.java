package com.luc.raizesdeserto.service;

import com.luc.raizesdeserto.domain.entity.Estoque;
import com.luc.raizesdeserto.domain.entity.ItemPedido;
import com.luc.raizesdeserto.domain.entity.Pagamento;
import com.luc.raizesdeserto.domain.entity.Pedido;
import com.luc.raizesdeserto.domain.enums.Status;
import com.luc.raizesdeserto.domain.enums.StatusPagamento;
import com.luc.raizesdeserto.dto.pagamento.PagamentoMockRequest;
import com.luc.raizesdeserto.repository.PagamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

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
     * Simula a solicitação de pagamento de um pedido junto a um gateway fictício (mock).
     *
     * <p>Valida se o pedido existe e se está com o status {@code AGUARDANDO_PAGAMENTO} antes
     * de registrar os dados do gateway e retornar um link de pagamento falso para fins de teste.
     *
     * @param pedido pedido a ser pago
     * @return link fictício de pagamento gerado pelo mock do gateway
     * @throws EntityNotFoundException  se nenhum pedido for encontrado com o {@code pedidoId} informado
     * @throws IllegalArgumentException se o pedido existir, mas seu status não for {@code AGUARDANDO_PAGAMENTO}
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
     * Registra o retorno de uma notificação de pagamento (webhook) para um pedido existente.
     *
     * <p>Valida se o pedido existe e se o pagamento ainda está com status {@code PENDENTE}
     * antes de processar o retorno. Caso o pagamento seja {@code APROVADO}, o pedido avança
     * para o status {@code EM_PREPARO}. Caso seja {@code RECUSADO}, apenas o payload e a
     * data de confirmação são registrados, sem alteração no status do pedido.
     *
     * @param request        do tipo PagamentoMockRequest.
     * @throws IllegalArgumentException se o pedido não for encontrado ou se o pagamento
     *                                  não estiver com status {@code PENDENTE}
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
     * Verifica e processa pedidos com timeout de pagamento.
     *
     * <p>Executado automaticamente a cada 10 segundos, este método busca todos os pedidos
     * que ainda estão aguardando confirmação de pagamento e os cancela por inatividade.
     * Para cada pedido afetado, o fluxo é:
     * <ol>
     *   <li>Atualiza o status do pedido para {@code CANCELADO}.</li>
     *   <li>Marca o pagamento associado com {@code StatusPagamento.TIMEOUT}.</li>
     *   <li>Estorna a quantidade de cada item de volta ao estoque da unidade de origem,
     *       caso o registro de estoque correspondente exista.</li>
     * </ol>
     *
     * <p>A operação inteira é executada dentro de uma transação ({@code @Transactional}),
     * garantindo consistência entre o cancelamento do pedido, a atualização do pagamento
     * e o crédito no estoque.
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
