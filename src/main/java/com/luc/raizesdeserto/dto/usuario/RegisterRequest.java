package com.luc.raizesdeserto.dto.usuario;

import com.luc.raizesdeserto.domain.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record RegisterRequest(@NotBlank(message = "Nome é obrigatório.")
                               @Size(min = 1, max = 150, message = "Nome deve estar entre 1 e 150 caracteres")
                               String nome,
                              @NotBlank(message = "Email é obrigatório.")
                               @Size(min = 1, max = 150, message = "Email deve estar entre 1 e 150 caracteres.")
                               @Email(message = "Email deve estar bem formado.")
                               String email,
                              @NotBlank(message = "Senha é obrigatória.")
                              @Size(min = 8, max = 50, message = "Senha deve estar entre 8 e 50 caracteres.")
                               String senha,
                              @AssertTrue(message = "Aceite de termos é obrigatório")
                              Boolean aceitouTermos,
                              Role role) {
}
