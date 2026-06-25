package com.luc.raizesnordeste.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AtualizarSenhaUsuarioRequest(
        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 8, max = 50, message = "Senha deve estar entre 8 e 50 caracteres.")
        String senha
) {
}
