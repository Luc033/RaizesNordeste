package com.luc.raizesdeserto.domain.entity;

import com.luc.raizesdeserto.domain.enums.FormaPagamento;
import com.luc.raizesdeserto.domain.enums.StatusPagamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"pedido"})
@EntityListeners(AuditingEntityListener.class)
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 50)
    private FormaPagamento formaPagamento;

    @ColumnDefault("'PENDENTE'")
    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento", nullable = false, length = 50)
    private StatusPagamento statusPagamento = StatusPagamento.PENDENTE;


    @Size(min = 1, max = 255, message = "Gateway pagamento deve ter entre 1 e 255 caracteres.")
    @Column(name = "gateway_pagamento", length = 255)
    private String gatewayPagamento;

    @Column(name = "payload_retorno", columnDefinition = "TEXT")
    private String payloadRetorno;

    @CreatedDate
    @Column(name = "solicitado_em", nullable = false)
    private LocalDateTime solicitadoEm;

    @Column(name = "confirmado_em")
    private LocalDateTime confirmadoEm;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;
}
