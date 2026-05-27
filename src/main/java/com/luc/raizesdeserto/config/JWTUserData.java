package com.luc.raizesdeserto.config;

import com.luc.raizesdeserto.domain.enums.Role;
import lombok.Builder;

import java.util.UUID;

@Builder
public record JWTUserData(UUID id, String email, Role role) {
}
