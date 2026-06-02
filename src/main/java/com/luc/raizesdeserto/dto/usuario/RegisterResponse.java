package com.luc.raizesdeserto.dto.usuario;

import lombok.Builder;

@Builder
public record RegisterResponse(String nome, String email) {
}
