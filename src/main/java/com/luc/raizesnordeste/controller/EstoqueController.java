package com.luc.raizesnordeste.controller;

import com.luc.raizesnordeste.domain.enums.MovimentacaoTipo;
import com.luc.raizesnordeste.dto.error.ErrorResponse;
import com.luc.raizesnordeste.dto.estoque.EstoqueResponse;
import com.luc.raizesnordeste.dto.estoque.MovimentacaoRequest;
import com.luc.raizesnordeste.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/estoques")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }


    @Operation(
            summary = "Lista o estoque de uma unidade",
            description = "Retorna a lista de itens de estoque (produto e quantidade atual) vinculados à unidade informada via parâmetro de consulta 'unidadeId'. Requer autenticação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estoque listado com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = EstoqueResponse.class)),
                            examples = @ExampleObject(
                                    name = "EstoqueListadoComSucesso",
                                    value = """
                                            [
                                              {
                                                "unidadeId": "d6bc413c-efb8-4786-9247-94259ae38cc1",
                                                "produtoId": "7f3e1a2b-5c6d-4b9e-8f1a-9c0d1e2f3a4b",
                                                "quantidadeAtual": 42
                                              },
                                              {
                                                "unidadeId": "d6bc413c-efb8-4786-9247-94259ae38cc1",
                                                "produtoId": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
                                                "quantidadeAtual": 15
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
                                              "timestamp": "2026-06-20T18:05:11.000",
                                              "path": "/estoques",
                                              "requestId": "4f6a7b8c-9d0e-41f2-8a3b-4c5d6e7f8a9b"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário autenticado não possui permissão para consultar o estoque.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AcessoNegado",
                                    value = """
                                            {
                                              "error": "Forbidden",
                                              "message": "Você não tem permissão para acessar este recurso.",
                                              "details": [],
                                              "timestamp": "2026-06-20T18:06:34.000",
                                              "path": "/estoques",
                                              "requestId": "8c9d0e1f-2a3b-4c4d-9e5f-6a7b8c9d0e1f"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping()
    public ResponseEntity<List<EstoqueResponse>> listarEstoquePorUnidade(@RequestParam UUID unidadeId) {
        var estoque = estoqueService.consultarPorUnidade(unidadeId).stream().map(e -> new EstoqueResponse(e)).toList();
        return ResponseEntity.ok().body(estoque);
    }



    @Operation(
            summary = "Lança uma movimentação de estoque",
            description = "Realiza crédito (ENTRADA) ou débito (SAIDA) de quantidade de um produto no estoque de uma unidade, de acordo com o tipo de movimentação informado em MovimentacaoRequest. Em caso de ENTRADA para um item ainda não cadastrado no estoque da unidade, um novo registro é criado vinculando Produto e Unidade. Acesso restrito a ADMIN e GERENTE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movimentação lançada com sucesso. A resposta não possui corpo."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida - campos obrigatórios ausentes/mal formatados ou quantidade a debitar maior do que o saldo disponível em estoque.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "ValidacaoCamposFalhou",
                                            value = """
                                                    {
                                                      "error": "Bad Request",
                                                      "message": "Erro na validação dos campos da requisição.",
                                                      "details": [
                                                        {
                                                          "field": "quantidade",
                                                          "issue": "Quantidade é obrigatória e deve ser maior que zero."
                                                        },
                                                        {
                                                          "field": "produtoID",
                                                          "issue": "ID do produto é obrigatório."
                                                        }
                                                      ],
                                                      "timestamp": "2026-06-20T18:09:27.000",
                                                      "path": "/estoques/movimentacao",
                                                      "requestId": "2e5f7a83-4c6d-4b9e-8f1a-6b7c8d9e0f1a"
                                                    }
                                                    """
                                    )
                            }
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
                                              "timestamp": "2026-06-20T18:12:03.000",
                                              "path": "/estoques/movimentacao",
                                              "requestId": "5a6b7c8d-9e0f-41a2-8b3c-4d5e6f7a8b9c"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário autenticado não possui a role ADMIN ou GERENTE exigida.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AcessoNegado",
                                    value = """
                                            {
                                              "error": "Forbidden",
                                              "message": "Você não tem permissão para acessar este recurso.",
                                              "details": [],
                                              "timestamp": "2026-06-20T18:13:19.000",
                                              "path": "/estoques/movimentacao",
                                              "requestId": "6b7c8d9e-0f1a-42b3-9c4d-5e6f7a8b9c0d"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto, Unidade ou registro de estoque correspondente não foram encontrados.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "EstoqueNaoEncontrado",
                                            value = """
                                                    {
                                                      "error": "Not Found",
                                                      "message": "Estoque não encontrado para o Produto 7f3e1a2b-5c6d-4b9e-8f1a-9c0d1e2f3a4b e Unidade d6bc413c-efb8-4786-9247-94259ae38cc1.",
                                                      "details": [],
                                                      "timestamp": "2026-06-20T18:14:55.000",
                                                      "path": "/estoques/movimentacao",
                                                      "requestId": "7c8d9e0f-1a2b-43c4-8d5e-6f7a8b9c0d1e"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "ProdutoOuUnidadeNaoEncontrados",
                                            value = """
                                                    {
                                                      "error": "Not Found",
                                                      "message": "Produto ID (7f3e1a2b-5c6d-4b9e-8f1a-9c0d1e2f3a4b) e/ou Unidade ID (d6bc413c-efb8-4786-9247-94259ae38cc1) não encontrados.",
                                                      "details": [],
                                                      "timestamp": "2026-06-20T18:15:40.000",
                                                      "path": "/estoques/movimentacao",
                                                      "requestId": "8d9e0f1a-2b3c-44d5-9e6f-7a8b9c0d1e2f"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "O registro de estoque foi modificado concorrentemente por outra operação antes da conclusão desta movimentação.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "ConflitoDeConcorrencia",
                                            value = """
                                            {
                                              "error": "Conflict",
                                              "message": "O recurso foi modificado por outra operação. Tente novamente.",
                                              "details": [],
                                              "timestamp": "2026-06-20T18:16:58.000",
                                              "path": "/estoques/movimentacao",
                                              "requestId": "9e0f1a2b-3c4d-45e6-8f7a-8b9c0d1e2f3a"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "EstoqueInsuficiente",
                                            value = """
                                                    {
                                                      "error": "Conflict",
                                                      "message": "Quantidade a debitar (10) é maior do que o saldo atual em estoque (3).",
                                                      "details": [],
                                                      "timestamp": "2026-06-20T18:10:48.000",
                                                      "path": "/estoques/movimentacao",
                                                      "requestId": "3f6a8b94-5d7e-4c0f-9a2b-7c8d9e0f1a2b"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PreAuthorize( "hasAnyRole('ADMIN', 'GERENTE')")
    @PostMapping("movimentacao")
    public ResponseEntity lancarMovimentacao(@Valid @RequestBody MovimentacaoRequest request){
        switch (request.tipo()){
            case MovimentacaoTipo.ENTRADA:
                estoqueService.creditar(request.produtoID(), request.unidadeId(), request.quantidade());
                return ResponseEntity.ok().build();
            case MovimentacaoTipo.SAIDA:
                estoqueService.debitar(request.produtoID(), request.unidadeId(), request.quantidade());
                return ResponseEntity.ok().build();
            default:
                return ResponseEntity.badRequest().build();
        }

    }

}
