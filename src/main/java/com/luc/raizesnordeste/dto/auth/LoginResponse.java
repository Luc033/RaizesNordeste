package com.luc.raizesnordeste.dto.auth;

public record LoginResponse(String token, Long expiresIn) {
}
