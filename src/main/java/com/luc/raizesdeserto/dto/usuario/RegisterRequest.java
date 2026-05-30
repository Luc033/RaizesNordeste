package com.luc.raizesdeserto.dto.usuario;

import com.luc.raizesdeserto.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank(message = "Nome não pode ser nulo.")
                               @Size(min = 1, max = 150, message = "Nome deve estar entre 1 e 150 caracteres")
                               String nome,
                              @NotBlank(message = "Email não pode ser nulo.")
                               @Size(min = 1, max = 150, message = "Email deve estar entre 1 e 150 caracteres.")
                               @Email(message = "Email deve estar bem formado.")
                               String email,
                              @NotBlank(message = "Senha não pode ser nula.")
                              @Size(min = 8, max = 50, message = "Senha deve estar entre 1 e 50 caracteres.")
                               String senha,
                              Role role) {
}
