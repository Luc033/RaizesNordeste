package com.luc.raizesnordeste.domain.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "item_pedido")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"pedido", "produto"})
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Positive(message = "Quantidade deve ser maior que zero")
    @NotNull(message = "Quantidade não pode ser nula.")
    @Column(nullable = false)
    private Integer quantidade;

    @PositiveOrZero(message = "Preço unitário deve ser igual ou maior que zero.")
    @NotNull(message = "Preço unitário não pode ser nulo.")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @JoinColumn(name = "pedido_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Pedido pedido;

    @JoinColumn(name = "produto_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Produto produto;

    public ItemPedido(Produto produto, Integer quantidade) {
        this.quantidade = quantidade;
        this.produto = produto;
    }
}
