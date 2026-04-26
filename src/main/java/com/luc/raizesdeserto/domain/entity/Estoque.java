package com.luc.raizesdeserto.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "estoque")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString(exclude = {"produto", "unidade"})
@NoArgsConstructor
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @PositiveOrZero(message = "Somente válido números positivos para quantidade atual.")
    @NotNull(message = "A quantidade atual não pode ser nula.")
    @ColumnDefault("0")
    @Column(name = "quantidade_atual", nullable = false)
    private Integer quantidadeAtual = 0;

    @PositiveOrZero(message = "Somente válido números positivos para quantidade mínima.")
    @NotNull(message = "A quantidade mínima não pode ser nula.")
    @ColumnDefault("0")
    @Column(name = "quantidade_minima", nullable = false)
    private Integer quantidadeMinima = 0;

    @LastModifiedDate
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

}
