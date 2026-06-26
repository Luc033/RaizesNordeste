package com.luc.raizesnordeste.controller;

import com.luc.raizesnordeste.dto.error.ErrorResponse;
import com.luc.raizesnordeste.dto.unidade.UnidadeResponse;
import com.luc.raizesnordeste.service.UnidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController()
@RequestMapping("/unidades")
public class UnidadeController {

    private final UnidadeService unidadeService;

    public UnidadeController(UnidadeService unidadeService) {
        this.unidadeService = unidadeService;
    }

    @Operation(
            summary = "Lista todas as unidades",
            description = "Retorna a lista completa de unidades cadastradas no sistema, incluindo nome, endereço, tipo de operação, horário de funcionamento e status de ativação. Endpoint de acesso público."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Unidades listadas com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = UnidadeResponse.class)),
                            examples = @ExampleObject(
                                    name = "UnidadesListadasComSucesso",
                                    value = """
                                            [
                                              {
                                                "id": "11b2c3d4-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
                                                "ativa": true,
                                                "nome": "Raízes do Nordeste - Centro",
                                                "endereco": "Av. Paulista, 1000 - São Paulo, SP",
                                                "tipoOperacao": "COZINHA_COMPLETA",
                                                "horarioAbertura": "08:00:00",
                                                "horarioFechamento": "22:00:00"
                                              },
                                              {
                                                "id": "22c3d4e5-6f7a-4b8c-9d0e-1f2a3b4c5d6e",
                                                "ativa": true,
                                                "nome": "Raízes do Nordeste - Shopping Sul",
                                                "endereco": "Rua das Flores, 250 - Curitiba, PR",
                                                "tipoOperacao": "FORMATO_REDUZIDO",
                                                "horarioAbertura": "10:00:00",
                                                "horarioFechamento": "21:00:00"
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado - token JWT ausente, expirado ou inválido.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "NaoAutenticado",
                                    value = """
                                            {
                                              "error": "Unauthorized",
                                              "message": "Token de acesso ausente, inválido ou expirado.",
                                              "details": [],
                                              "timestamp": "2026-06-20T19:20:57.000",
                                              "path": "/unidades",
                                              "requestId": "a4d5f061-c749-4960-b832-d7e8f9a0b1c2"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping()
    public ResponseEntity<List<UnidadeResponse>> listarTodasUnidades() {
        var unidades = unidadeService.listar().stream().map(u -> new UnidadeResponse(u)).toList();
        return ResponseEntity.ok().body(unidades);
    }


    @Operation(
            summary = "Busca uma unidade pelo identificador",
            description = "Retorna os dados completos de uma unidade cadastrada (nome, endereço, tipo de operação, horários de funcionamento e status de ativação) a partir do seu ID. Endpoint de acesso público."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Unidade encontrada com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UnidadeResponse.class),
                            examples = @ExampleObject(
                                    name = "UnidadeEncontrada",
                                    value = """
                                            {
                                              "id": "11b2c3d4-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
                                              "ativa": true,
                                              "nome": "Raízes do Nordeste - Centro",
                                              "endereco": "Av. Paulista, 1000 - São Paulo, SP",
                                              "tipoOperacao": "COZINHA_COMPLETA",
                                              "horarioAbertura": "08:00:00",
                                              "horarioFechamento": "22:00:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Unidade não encontrada para o ID informado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UnidadeNaoEncontrada",
                                    value = """
                                            {
                                              "error": "Not Found",
                                              "message": "Unidade não encontrada: 11b2c3d4-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
                                              "details": [],
                                              "timestamp": "2026-06-20T11:55:18.000",
                                              "path": "/unidades/11b2c3d4-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
                                              "requestId": "1b4c6d72-3f5a-4e8b-c20d-4e5f6a7b8c9d"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("{unidadeId}")
    public ResponseEntity<UnidadeResponse> buscarUnidade(@PathVariable UUID unidadeId) {
        return ResponseEntity.ok().body(new UnidadeResponse(unidadeService.buscarPorId(unidadeId)));
    }

}
