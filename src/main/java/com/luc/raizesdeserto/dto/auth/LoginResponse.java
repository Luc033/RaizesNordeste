package com.luc.raizesdeserto.dto.auth;

public record LoginResponse(String token, Long expiresIn) {
}
