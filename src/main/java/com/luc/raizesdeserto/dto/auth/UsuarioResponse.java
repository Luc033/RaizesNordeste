package com.luc.raizesdeserto.dto.auth;

import com.luc.raizesdeserto.domain.enums.Role;

import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, String email, Role role) {
}
