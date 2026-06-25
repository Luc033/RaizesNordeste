package com.luc.raizesnordeste.domain.entity;

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

/**
 * Entidade de consentimento para conformidade com a LGPD.
 * Armazena o registro de aceite do usuário para finalidades específicas.
 *
 * Anotações:
 * - {@code @Entity}, {@code @Table}: Mapeamento JPA para a tabela "consentimento_lgpd".
 * - {@code @Getter}, {@code @Setter}, {@code @NoArgsConstructor}: Configurações do Lombok.
 * - {@code @ToString}: Ignora a exibição de "usuarioId" para evitar problemas de lazy loading.
 * - {@code @EntityListeners}: Habilita auditoria de eventos do ciclo de vida.
 *
 * Campos:
 * - {@code id} (UUID): Identificador único do registro. Gerado automaticamente.
 * - {@code finalidade} (String): Propósito do consentimento. Obrigatório.
 * - {@code aceitou} (Boolean): Status de aceitação dos termos. Obrigatório.
 * - {@code dataAceite} (Timestamp): Data e hora do consentimento. Gerado automaticamente.
 * - {@code ipOrigem} (String): Endereço IP de onde o aceite foi feito. Opcional.
 * - {@code usuario} (Usuario): Relacionamento Many-to-one com o usuário responsável.
 */

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

    @NotBlank(message = "Finalidade não pode ser nula.")
    @Size(min = 1, max = 100, message = "Finalidade deve estar entre 1 e 100 caracteres.")
    @Column(length = 100, nullable = false)
    private String finalidade;

    @NotNull(message = "Aceite não pode ser nulo.")
    @Column(nullable = false)
    private Boolean aceitou;

    @CreatedDate
    @Column(name = "data_aceite", nullable = false, updatable = false)
    private LocalDateTime dataAceite;

    @Size(min = 1, max = 45, message = "IP deve estar entre 1 e 45 caracteres.")
    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
