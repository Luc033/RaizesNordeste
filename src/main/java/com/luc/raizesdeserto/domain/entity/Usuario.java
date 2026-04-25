package com.luc.raizesdeserto.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity()
@Table(name = "usuario")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "O nome não pode ser nulo.")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
    @Column(length = 150, nullable = false)
    private String nome;

    @NotBlank(message = "O email não pode ser nulo.")
    @Size(max = 150, message = "O email deve ter no máximo 150 caracteres.")
    @Email(message = "Deve ser um endereço de e-mail bem formado.")
    @Column(length = 150, unique = true)
    private String email;

    @NotBlank(message = "A senha não pode ser nula.")
    @Size( min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
    @Column( name = "senha_hash", length = 255, nullable = false)
    private String senhaHash;

    @NotNull(message = "O campo ativo não pode ser nulo.")
    @ColumnDefault("true")
    @Column(nullable = false)
    private Boolean ativo = true;

    @CreatedDate
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;





}
