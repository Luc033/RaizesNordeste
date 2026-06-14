package com.luc.raizesdeserto.service;

import com.luc.raizesdeserto.domain.entity.LogStatusPedido;
import com.luc.raizesdeserto.domain.entity.Pedido;
import com.luc.raizesdeserto.domain.entity.Usuario;
import com.luc.raizesdeserto.domain.enums.Status;
import com.luc.raizesdeserto.repository.LogStatusPedidoRepository;
import com.luc.raizesdeserto.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuditoriaService {

    private final LogStatusPedidoRepository logStatusPedidoRepository;

    public AuditoriaService(LogStatusPedidoRepository logStatusPedidoRepository) {
        this.logStatusPedidoRepository = logStatusPedidoRepository;
    }


    /**
     * Registra uma transição de status de um pedido no histórico de auditoria.
     *
     * <p>Cria e persiste um {@link LogStatusPedido} contendo o estado anterior e o novo
     * estado do pedido, o usuário responsável pela mudança e uma observação opcional,
     * permitindo rastrear todo o ciclo de vida do pedido.
     *
     * @param pedido       o pedido cujo status foi alterado
     * @param usuario      o usuário responsável pela transição de status
     * @param statusAntigo o status em que o pedido se encontrava antes da alteração
     * @param novoStatus   o novo status atribuído ao pedido
     * @param observacao   comentário ou justificativa sobre a mudança de status; pode ser {@code null}
     */
    @Transactional
    public void registrarTransicao(Pedido pedido, Usuario usuario, Status statusAntigo, Status novoStatus, String observacao) {
        LogStatusPedido log = new LogStatusPedido();
        log.setPedido(pedido);
        log.setUsuarioResponsavel(usuario);
        log.setStatusAnterior(statusAntigo);
        log.setStatusNovo(novoStatus);
        log.setObservacao(observacao);
        logStatusPedidoRepository.save(log);
    }
}
