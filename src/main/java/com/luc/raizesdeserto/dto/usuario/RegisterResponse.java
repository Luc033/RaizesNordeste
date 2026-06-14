package com.luc.raizesdeserto.dto.usuario;

import com.luc.raizesdeserto.domain.entity.Usuario;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RegisterResponse(UUID id, String nome, String email) {
    public RegisterResponse(Usuario u){
        this(u.getId(), u.getNome(), u.getEmail());
    }
}
