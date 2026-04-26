package com.luc.raizesdeserto.domain.entity;

import com.luc.raizesdeserto.domain.enums.TipoOperacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "unidade")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Unidade {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(min = 1, max = 100, message = "O nome deve estar entre 1 e 100 caracteres.")
    @NotBlank(message = "O nome não pode ser nulo.")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Size(min = 1, max = 255, message = "O endereço deve estar entre 1 e 255 caracteres.")
    @NotBlank(message = "O endereço não pode ser nulo.")
    @Column(name = "endereco", nullable = false, length = 255)
    private String endereco;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacao", nullable = false,  length = 30)
    @ColumnDefault("'COZINHA_COMPLETA'")
    private TipoOperacao tipoOperacao = TipoOperacao.COZINHA_COMPLETA;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean ativa = true;

    @Column(name = "horario_abertura", nullable = false)
    private LocalDate horarioAbertura;

    @Column(name = "horario_fechamento", nullable = false)
    private LocalDate horarioFechamento;


}
