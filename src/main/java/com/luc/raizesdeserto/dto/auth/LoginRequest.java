package com.luc.raizesdeserto.dto.auth;

import jakarta.validation.constraints.NotEmpty;

public record LoginRequest(@NotEmpty(message = "email é obrigatório") String email,
                           @NotEmpty(message = "senha é obrigatório") String senha) {
}
