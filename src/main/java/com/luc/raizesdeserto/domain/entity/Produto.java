package com.luc.raizesdeserto.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "produto")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome não pode ser nulo.")
    @Size(min = 1, max = 100, message = "Nome deve estar entre 1 e 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "Preço base não pode ser nulo.")
    @PositiveOrZero(message = "Preço base deve ser igual ou maior que zero.")
    @Column(name = "preco_base", nullable = false, precision =  10, scale = 2)
    private BigDecimal precoBase;

    @Size(min = 1, max = 80, message = "Categoria deve estar entre 1 e 80 caracteres.")
    @Column(length = 80)
    private String categoria;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean sazonal = false;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean ativo = true;

    public Produto(UUID id) {
        this.id = id;
    }
}
