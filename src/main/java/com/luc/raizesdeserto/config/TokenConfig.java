package com.luc.raizesdeserto.config;

import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.luc.raizesdeserto.domain.entity.Usuario;
import com.luc.raizesdeserto.domain.enums.Role;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class TokenConfig {

    private String secret = "secret";


    /**
     * Gera um token JWT para o usuário fornecido.
     *
     * @param usuario o usuário para o qual o token será gerado.
     * Ele contém atributos como ID, e-mail e role
     * que serão codificados como claims no token.
     * @return um token JWT assinado como uma string, contendo o ID do usuário, role, e-mail como sujeito,
     * e metadados adicionais como timestamps de expiração e emissão.
     */
    public String gerarToken(Usuario usuario) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withClaim("usuarioId", usuario.getId().toString())
                .withClaim("role", usuario.getRole().name())
                .withSubject(usuario.getEmail())
                .withExpiresAt(Instant.now().plusSeconds(28000))
                .withIssuedAt(Instant.now())
                .sign(algorithm);
    }

    /**
     * Valida um token JWT fornecido e extrai os dados do usuário se o token for válido.
     *
     * @param token o token JWT a ser validado e analisado.
     * @return um {@code Optional<JWTUserData>} contendo os dados do usuário extraídos
     * do token se ele for válido. Retorna um {@code Optional} vazio se o
     * token for inválido ou a verificação falhar.
     */
    public Optional<JWTUserData> validateToken(String token) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT decode = JWT.require(algorithm).build().verify(token);

            String role = decode.getClaim("role").asString();
            Role userRole = Role.valueOf(role);

            return Optional.of(JWTUserData.builder()
                    .id(decode.getClaim("usuarioId").as(UUID.class))
                    .email(decode.getSubject())
                    .role(userRole)
                    .build());

        }catch (JWTVerificationException e){
            return Optional.empty();
        }
    }

}
