package com.luc.raizesdeserto.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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

    @NotBlank(message = "O nome não pode ser nulo.")
    @Size(min = 1, max = 100, message = "O nome deve estar entre 1 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;


    private BigDecimal precoBase;
    private String categoria;
    private Boolean sazonal;
    private Boolean ativo;
}
