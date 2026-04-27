package com.luc.raizesdeserto.domain.entity;

import com.luc.raizesdeserto.domain.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "log_status_pedido")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"usuarioResponsavel", "pedido"})
@EntityListeners(AuditingEntityListener.class)
public class LogStatusPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 50)
    private Status statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", length = 50, nullable = false)
    private Status statusNovo;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @CreatedDate
    @Column(name = "atualizado_em", nullable = false, updatable = false)
    private LocalDateTime atualizadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario usuarioResponsavel;
}
