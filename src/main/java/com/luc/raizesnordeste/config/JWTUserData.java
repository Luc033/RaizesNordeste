package com.luc.raizesnordeste.config;

import com.luc.raizesnordeste.domain.enums.Role;
import lombok.Builder;

import java.util.UUID;

@Builder
public record JWTUserData(UUID id, String email, Role role) {
}
