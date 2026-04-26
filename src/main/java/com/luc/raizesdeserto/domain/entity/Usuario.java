package com.luc.raizesdeserto.domain.entity;

import com.luc.raizesdeserto.domain.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity()
@Table(name = "usuario")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "O nome não pode ser nulo.")
    @Size(min = 1, max = 150, message = "O nome deve estar entre 1 e 150 caracteres")
    @Column(length = 150, nullable = false)
    private String nome;

    @NotBlank(message = "O email não pode ser nulo.")
    @Size(min = 1, max = 150, message = "O email deve estar entre 1 e 150 caracteres.")
    @Email(message = "Deve ser um endereço de e-mail bem formado.")
    @Column(length = 150, unique = true)
    private String email;

    @NotNull(message = "A senha não pode ser nula.")
    @Size(min = 1, max = 8, message = "A senha deve estar entre 1 e 8 caracteres.")
    @Column( name = "senha_hash", length = 255, nullable = false)
    private String senhaHash;

    @NotNull(message = "O campo ativo não pode ser nulo.")
    @ColumnDefault("true")
    @Column(nullable = false)
    private Boolean ativo = true;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @LastModifiedDate
    @Column(name = "atualizado_em", nullable = false, updatable = false)
    private LocalDateTime atualizadoEm;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Role role;
}
