package com.luc.raizesnordeste.infra.swagger;

import com.luc.raizesnordeste.dto.error.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "409",
                description = "Conflito - violação de integridade dos dados ou concorrência de modificação.",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "RegistroDuplicado",
                                        value = """
                                                {
                                                  "error": "Conflict",
                                                  "message": "Já existe um registro com esses dados.",
                                                  "details": [],
                                                  "timestamp": "2026-01-15T10:30:00",
                                                  "path": "/pedidos",
                                                  "requestId": "550e8400-e29b-41d4-a716-446655440000"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "ConflitoConcorrencia",
                                        value = """
                                                {
                                                  "error": "Conflict",
                                                  "message": "O recurso foi modificado por outra operação. Tente novamente.",
                                                  "details": [],
                                                  "timestamp": "2026-01-15T10:30:00",
                                                  "path": "/caminho/da/requisicao",
                                                  "requestId": "550e8400-e29b-41d4-a716-446655440000"
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface ConflictApiErrors {
}