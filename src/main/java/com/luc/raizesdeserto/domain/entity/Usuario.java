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
import java.util.List;
import java.util.UUID;

@Entity()
@Table(name = "usuario")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString(exclude = {"logStatusPedido"})
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome não pode ser nulo.")
    @Size(min = 1, max = 150, message = "Nome deve estar entre 1 e 150 caracteres")
    @Column(length = 150, nullable = false)
    private String nome;

    @NotBlank(message = "Email não pode ser nulo.")
    @Size(min = 1, max = 150, message = "Email deve estar entre 1 e 150 caracteres.")
    @Email(message = "Email deve estar bem formado.")
    @Column(length = 150, unique = true)
    private String email;

    @NotBlank(message = "Senha não pode ser nula.")
    @Column( name = "senha_hash", length = 255, nullable = false)
    private String senhaHash;

    @NotNull(message = "Ativo não pode ser nulo.")
    @ColumnDefault("true")
    @Column(nullable = false)
    private Boolean ativo = true;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @LastModifiedDate
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Role role;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuarioResponsavel")
    private List<LogStatusPedido> logStatusPedido;
}
