package com.luc.raizesnordeste.controller;

import com.luc.raizesnordeste.dto.error.ErrorResponse;
import com.luc.raizesnordeste.dto.pagamento.PagamentoMockRequest;
import com.luc.raizesnordeste.service.PagamentoService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }


    @Operation(
            summary = "Processa o retorno (callback) de um pagamento simulado",
            description = "Recebe a notificação de retorno do gateway de pagamento mock (PagamentoMockRequest) referente a um pedido. Caso o status seja APROVADO, confirma o pagamento e avança o pedido para EM_PREPARO; caso seja RECUSADO, apenas registra o payload e a data de confirmação, sem alterar o status do pedido. Acesso restrito a usuários com role ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Retorno de pagamento processado com sucesso. A resposta não possui corpo."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida - campos obrigatórios ausentes/mal formatados ou o pagamento do pedido não está com status PENDENTE para receber o retorno.",
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
                                                          "field": "pedidoId",
                                                          "issue": "pedidoId é obrigatório."
                                                        },
                                                        {
                                                          "field": "statusPagamento",
                                                          "issue": "statusPagamento é obrigatório"
                                                        }
                                                      ],
                                                      "timestamp": "2026-06-20T18:25:14.000",
                                                      "path": "/pagamentos/mock/callback",
                                                      "requestId": "a1b2c3d4-5e6f-47a8-9b0c-1d2e3f4a5b6c"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "PagamentoNaoEstaPendente",
                                            value = """
                                                    {
                                                      "error": "Bad Request",
                                                      "message": "Não é possível registrar o retorno do pagamento. Status atual: APROVADO",
                                                      "details": [],
                                                      "timestamp": "2026-06-20T18:26:37.000",
                                                      "path": "/pagamentos/mock/callback",
                                                      "requestId": "b2c3d4e5-6f7a-48b9-9c0d-2e3f4a5b6c7d"
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
                                              "timestamp": "2026-06-20T18:27:49.000",
                                              "path": "/pagamentos/mock/callback",
                                              "requestId": "c3d4e5f6-7a8b-49c0-9d1e-3f4a5b6c7d8e"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário autenticado não possui a role ADMIN exigida.",
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
                                              "timestamp": "2026-06-20T18:28:55.000",
                                              "path": "/pagamentos/mock/callback",
                                              "requestId": "d4e5f6a7-8b9c-40d1-9e2f-4a5b6c7d8e9f"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhum pedido foi encontrado com o pedidoId informado no corpo da requisição.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "PedidoNaoEncontrado",
                                    value = """
                                            {
                                              "error": "Not Found",
                                              "message": "Pedido não encontrado: 7f3e1a2b-5c6d-4b9e-8f1a-9c0d1e2f3a4b",
                                              "details": [],
                                              "timestamp": "2026-06-20T18:29:42.000",
                                              "path": "/pagamentos/mock/callback",
                                              "requestId": "e5f6a7b8-9c0d-41e2-9f3a-5b6c7d8e9f0a"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize( "hasRole('ROLE_ADMIN')")
    @PostMapping("mock/callback")
    public ResponseEntity<Void> processarRetornoPagamento(
            @Valid @RequestBody PagamentoMockRequest request
            ){
        pagamentoService.registrarRetorno(request);
        return ResponseEntity.ok().build();
    }

}