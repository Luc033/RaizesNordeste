package com.luc.raizesdeserto.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consentimento_lgpd")
@Getter
@Setter
@ToString(exclude = {"usuarioId"})
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ConsentimentoLGPD {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "A finalidade não pode ser nula.")
    @Size(min = 1, max = 100, message = "A finalidade deve estar entre 1 e 100 caracteres.")
    @Column(length = 100, nullable = false)
    private String finalidade;

    @NotNull(message = "O aceite não pode ser nulo.")
    @Column(nullable = false)
    private Boolean aceitou;

    @CreatedDate
    @Column(name = "data_aceite", nullable = false, updatable = false)
    private LocalDateTime dataAceite;

    @Size(min = 1, max = 45, message = "O IP deve estar entre 1 e 45 caracteres.")
    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
