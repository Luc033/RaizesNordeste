package com.luc.raizesnordeste.controller;

import com.luc.raizesnordeste.config.JWTUserData;
import com.luc.raizesnordeste.domain.entity.*;
import com.luc.raizesnordeste.domain.enums.CanalPedido;
import com.luc.raizesnordeste.dto.error.ErrorResponse;
import com.luc.raizesnordeste.dto.pedido.AtualizarStatusRequest;
import com.luc.raizesnordeste.dto.pedido.CriarPedidoRequest;
import com.luc.raizesnordeste.dto.pedido.PedidoPagamentoResponse;
import com.luc.raizesnordeste.dto.pedido.PedidoResponse;
import com.luc.raizesnordeste.infra.GatewayPagamentoClient;
import com.luc.raizesnordeste.service.PagamentoService;
import com.luc.raizesnordeste.service.PedidoService;
import com.luc.raizesnordeste.service.UnidadeService;
import com.luc.raizesnordeste.service.UsuarioService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("pedidos")
public class PedidoController {
    private final PedidoService pedidoService;
    private final UnidadeService unidadeService;
    private final UsuarioService usuarioService;
    private final GatewayPagamentoClient pagamentoClient;
    private final PagamentoService pagamentoService;


    public PedidoController(PedidoService pedidoService, UnidadeService unidadeService, UsuarioService usuarioService, GatewayPagamentoClient pagamentoClient, PagamentoService pagamentoService) {
        this.pedidoService = pedidoService;
        this.unidadeService = unidadeService;
        this.usuarioService = usuarioService;
        this.pagamentoClient = pagamentoClient;
        this.pagamentoService = pagamentoService;
    }


    @Operation(
            summary = "Cria um novo pedido",
            description = "Cria um pedido para o usuário autenticado (CriarPedidoRequest), valida o canal informado, a existência e status ativo de cada produto, a disponibilidade de estoque na unidade e aplica integridade de preço (utilizando sempre o precoBase cadastrado no banco). Após salvar o pedido com status AGUARDANDO_PAGAMENTO, envia a solicitação ao gateway de pagamento mock e retorna o pedido junto do link de pagamento gerado. Acesso restrito a CLIENTE e ATENDENTE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido criado com sucesso, junto do link de pagamento gerado pelo gateway.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PedidoPagamentoResponse.class),
                            examples = @ExampleObject(
                                    name = "PedidoCriadoComSucesso",
                                    value = """
                                            {
                                              "pedidoId": "3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "status": "AGUARDANDO_PAGAMENTO",
                                              "canal": "WEB",
                                              "valorTotal": 55.00,
                                              "itens": [
                                                {
                                                  "produtoID": "7f3e1a2b-5c6d-4b9e-8f1a-9c0d1e2f3a4b",
                                                  "nome": "Hambúrguer Artesanal",
                                                  "quantidade": 2,
                                                  "valorUnitario": 32.50
                                                },
                                                {
                                                  "produtoID": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
                                                  "nome": "Refrigerante Lata",
                                                  "quantidade": 1,
                                                  "valorUnitario": 22.50
                                                }
                                              ],
                                              "infoPagamento": "https://pagamento-mock.com.br/pay/3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "criadoEm": "2026-06-20T18:40:12.000"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida - campos obrigatórios ausentes/mal formatados, canal de pedido inválido, usuário inexistente ou produto inativo.",
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
                                                          "field": "itens",
                                                          "issue": "O pedido deve conter pelo menos um item."
                                                        },
                                                        {
                                                          "field": "formaPagamento",
                                                          "issue": "Forma de pagamento é obrigatória."
                                                        }
                                                      ],
                                                      "timestamp": "2026-06-20T18:41:05.000",
                                                      "path": "/pedidos",
                                                      "requestId": "4a7b9c05-6e8f-4d1a-8b3c-8d9e0f1a2b3c"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "ProdutoInativo",
                                            value = """
                                                    {
                                                      "error": "Bad Request",
                                                      "message": "Produto inativo: ItemPedido{produto=Hambúrguer Artesanal, quantidade=2}",
                                                      "details": [],
                                                      "timestamp": "2026-06-20T18:42:18.000",
                                                      "path": "/pedidos",
                                                      "requestId": "5b8c0d16-7f9a-4e2b-9c4d-9e0f1a2b3c4d"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "CanalInvalido",
                                            value = """
                                                    {
                                                      "error": "Bad Request",
                                                      "message": "Canal de novoPedido inválido: null",
                                                      "details": [],
                                                      "timestamp": "2026-06-20T18:43:29.000",
                                                      "path": "/pedidos",
                                                      "requestId": "6c9d1e27-8a0b-4f3c-ad5e-9f0a1b2c3d4e"
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
                                              "timestamp": "2026-06-20T18:44:37.000",
                                              "path": "/pedidos",
                                              "requestId": "7d0e2f38-9b1c-4d4e-be6f-0a1b2c3d4e5f"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário autenticado não possui a role CLIENTE ou ATENDENTE exigida.",
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
                                              "timestamp": "2026-06-20T18:45:49.000",
                                              "path": "/pedidos",
                                              "requestId": "8e1f3a49-0c2d-4e5f-9c70-1b2c3d4e5f60"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado, ou a combinação de produto e unidade não possui registro de estoque.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "ItemNaoEncontrado",
                                            value = """
                                                    {
                                                      "error": "Not Found",
                                                      "message": "Item não encontrado: ItemPedido{produto=7f3e1a2b-5c6d-4b9e-8f1a-9c0d1e2f3a4b, quantidade=2}",
                                                      "details": [],
                                                      "timestamp": "2026-06-20T18:46:52.000",
                                                      "path": "/pedidos",
                                                      "requestId": "9f2a4b50-1d3e-4f60-8d81-2c3d4e5f6071"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "ProdutoOuUnidadeNaoEncontrados",
                                            value = """
                                                    {
                                                      "error": "Not Found",
                                                      "message": "Produto (7f3e1a2b-5c6d-4b9e-8f1a-9c0d1e2f3a4b) e/ou Unidade (d6bc413c-efb8-4786-9247-94259ae38cc1) não encontrados.",
                                                      "details": [],
                                                      "timestamp": "2026-06-20T18:47:14.000",
                                                      "path": "/pedidos",
                                                      "requestId": "a03b5c61-2e4f-4071-9e92-3d4e5f607182"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Estoque insuficiente para atender a quantidade solicitada de um dos itens do pedido.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "EstoqueInsuficiente",
                                    value = """
                                            {
                                              "error": "Conflict",
                                              "message": "Estoque insuficiente no item [1]: Hambúrguer Artesanal (3f2504e0-4f89-11d3-9a0c-0305e82c3301) - Qtd pedida: 5",
                                              "details": [],
                                              "timestamp": "2026-06-20T18:48:36.000",
                                              "path": "/pedidos",
                                              "requestId": "b14c6d72-3f50-4182-8fa3-4e5f60718293"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_ATENDENTE')")
    public ResponseEntity<PedidoPagamentoResponse> criarPedido(@Valid @RequestBody CriarPedidoRequest request,
                                                               @AuthenticationPrincipal JWTUserData usuario){
        Pagamento pagamento = new Pagamento();
        pagamento.setFormaPagamento(request.formaPagamento());
        Pedido pedido = new Pedido();
        List<ItemPedido> itens = new ArrayList<>(request.itens().stream()
                .map(i -> new ItemPedido(new Produto(i.produtoId()), i.quantidade())).toList());
        pedido.setUnidade(unidadeService.buscarPorId(request.unidadeId()));
        pedido.setCanalPedido(request.canalPedido());
        pedido.adicionarItens(itens);
        pedido.setUsuario(usuarioService.buscarPorId(usuario.id()).get());
        pedido.setPagamento(pagamento);


        // pedido criado
        var pedidoSalvo = pedidoService.criarPedido(pedido);

        // envia o pedido para o gateway de pagamento e o mesmo retorna o link de pagamento
        var response = pagamentoClient.enviarSolicitacaoPagamentoMock(pedidoSalvo);

        // retorna ao cliente o pedido criado junto do link de pagamento
        return ResponseEntity.ok().body(new PedidoPagamentoResponse(pedidoSalvo, response)) ;


    }


    @Operation(
            summary = "Busca um pedido pelo identificador",
            description = "Retorna os dados completos de um pedido (status, canal, valor total, itens e data de criação) a partir do seu ID. Requer autenticação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido encontrado com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Pedido.class),
                            examples = @ExampleObject(
                                    name = "PedidoEncontrado",
                                    value = """
                                            {
                                              "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                                              "status": "EM_PREPARO",
                                              "canalPedido": "WEB",
                                              "valorTotal": 87.50,
                                              "criadoEm": "2026-06-20T18:30:00.000",
                                              "itens": [
                                                {
                                                  "produto": {
                                                    "id": "7f3e1a2b-5c6d-4b9e-8f1a-9c0d1e2f3a4b",
                                                    "nome": "Hambúrguer Artesanal"
                                                  },
                                                  "quantidade": 2,
                                                  "precoUnitario": 32.50
                                                }
                                              ]
                                            }
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
                                              "timestamp": "2026-06-20T18:50:41.000",
                                              "path": "/pedidos/7c9e6679-7425-40de-944b-e07fc1f90ae7",
                                              "requestId": "c25d7e83-4061-4293-90b4-5f6071829304"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado para o ID informado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "PedidoNaoEncontrado",
                                    value = """
                                            {
                                              "error": "Not Found",
                                              "message": "Pedido não encontrado: 7c9e6679-7425-40de-944b-e07fc1f90ae7",
                                              "details": [],
                                              "timestamp": "2026-06-20T18:51:53.000",
                                              "path": "/pedidos/7c9e6679-7425-40de-944b-e07fc1f90ae7",
                                              "requestId": "d36e8f94-5172-4304-91c5-60718293045a"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("{id}")
    public Pedido buscarPedido(@PathVariable UUID id){
        return pedidoService.buscarPorId(id);
    }


    @Operation(
            summary = "Atualiza o status de um pedido",
            description = "Altera o status do pedido informado (AtualizarStatusRequest), validando se o novo status é um valor reconhecido do enum Status e registrando a transição na auditoria, podendo incluir uma observação sobre a mudança. Acesso restrito a ADMIN, GERENTE, COZINHA e ATENDENTE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status do pedido atualizado com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PedidoResponse.class),
                            examples = @ExampleObject(
                                    name = "StatusAtualizadoComSucesso",
                                    value = """
                                            {
                                              "pedidoId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                                              "status": "EM_PREPARO",
                                              "canal": "WEB",
                                              "valorTotal": 65.00,
                                              "itens": [
                                                {
                                                  "produtoID": "7f3e1a2b-5c6d-4b9e-8f1a-9c0d1e2f3a4b",
                                                  "nome": "Hambúrguer Artesanal",
                                                  "quantidade": 2,
                                                  "valorUnitario": 32.50
                                                }
                                              ],
                                              "criadoEm": "2026-06-20T18:30:00.000"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida - corpo da requisição ausente, mal formatado ou campo obrigatório não informado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ValidacaoCamposFalhou",
                                    value = """
                                            {
                                              "error": "Bad Request",
                                              "message": "Erro na validação dos campos da requisição.",
                                              "details": [
                                                {
                                                  "field": "status",
                                                  "issue": "Status é obrigatório."
                                                }
                                              ],
                                              "timestamp": "2026-06-20T18:53:06.000",
                                              "path": "/pedidos/7c9e6679-7425-40de-944b-e07fc1f90ae7/status",
                                              "requestId": "e47f9a05-6283-4415-92d6-718293045a6b"
                                            }
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
                                              "timestamp": "2026-06-20T18:54:18.000",
                                              "path": "/pedidos/7c9e6679-7425-40de-944b-e07fc1f90ae7/status",
                                              "requestId": "f580ab16-7394-4526-93e7-8293045a6b7c"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário autenticado não possui nenhuma das roles ADMIN, GERENTE, COZINHA ou ATENDENTE.",
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
                                              "timestamp": "2026-06-20T18:55:29.000",
                                              "path": "/pedidos/7c9e6679-7425-40de-944b-e07fc1f90ae7/status",
                                              "requestId": "0691bc27-8405-4637-94f8-93045a6b7c8d"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado para o ID informado, ou o status enviado não corresponde a um valor reconhecido do enum Status.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "PedidoNaoEncontrado",
                                            value = """
                                                    {
                                                      "error": "Not Found",
                                                      "message": "Pedido não encontrado: 7c9e6679-7425-40de-944b-e07fc1f90ae7",
                                                      "details": [],
                                                      "timestamp": "2026-06-20T18:56:47.000",
                                                      "path": "/pedidos/7c9e6679-7425-40de-944b-e07fc1f90ae7/status",
                                                      "requestId": "17a2cd38-9516-4748-a509-045a6b7c8d9e"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "StatusNaoEncontrado",
                                            value = """
                                                    {
                                                      "error": "Not Found",
                                                      "message": "Novo status do pedido não encontrado: FINALIZADO_TESTE",
                                                      "details": [],
                                                      "timestamp": "2026-06-20T18:57:59.000",
                                                      "path": "/pedidos/7c9e6679-7425-40de-944b-e07fc1f90ae7/status",
                                                      "requestId": "28b3de49-0627-4859-b610-15a6b7c8d9e0"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "O pedido foi modificado concorrentemente por outra operação antes da conclusão desta atualização.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ConflitoDeConcorrencia",
                                    value = """
                                            {
                                              "error": "Conflict",
                                              "message": "O recurso foi modificado por outra operação. Tente novamente.",
                                              "details": [],
                                              "timestamp": "2026-06-20T18:59:11.000",
                                              "path": "/pedidos/7c9e6679-7425-40de-944b-e07fc1f90ae7/status",
                                              "requestId": "39c4ef50-1738-496a-c721-26b7c8d9e0f1"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GERENTE', 'ROLE_COZINHA', 'ROLE_ATENDENTE')")
    @PatchMapping("{id}/status")
    public ResponseEntity<PedidoResponse> atualizarStatus(@PathVariable UUID id,
                                                          @Valid @RequestBody AtualizarStatusRequest request,
                                                          @AuthenticationPrincipal Usuario usuario){
        var pedidoAtualizado = pedidoService.atualizarStatus(usuario, id, request.status(), request.observacao());
        return ResponseEntity.ok().body(new PedidoResponse(pedidoAtualizado));

    }


    @Operation(
            summary = "Lista pedidos por canal",
            description = "Retorna a lista de pedidos filtrados pelo canal de pedido informado via parâmetro de consulta 'canalPedido' (ex: APP, TOTEM, BALCAO). Acesso restrito a ADMIN, GERENTE, COZINHA e ATENDENTE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedidos listados com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = PedidoResponse.class)),
                            examples = @ExampleObject(
                                    name = "PedidosListadosComSucesso",
                                    value = """
                                            [
                                              {
                                                "pedidoId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                                                "status": "EM_PREPARO",
                                                "canal": "WEB",
                                                "valorTotal": 87.50,
                                                "itens": [
                                                  {
                                                    "produtoID": "7f3e1a2b-5c6d-4b9e-8f1a-9c0d1e2f3a4b",
                                                    "nome": "Hambúrguer Artesanal",
                                                    "quantidade": 2,
                                                    "valorUnitario": 32.50
                                                  }
                                                ],
                                                "criadoEm": "2026-06-20T18:30:00.000"
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida - o parâmetro 'canalPedido' está ausente ou não corresponde a um valor válido do enum CanalPedido.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CanalPedidoInvalido",
                                    value = """
                                            {
                                              "error": "Bad Request",
                                              "message": "O parâmetro 'canalPedido' deve ser um dos seguintes valores: APP, TOTEM, BALCAO, PICKUP, WEB.",
                                              "details": [],
                                              "timestamp": "2026-06-20T19:00:23.000",
                                              "path": "/pedidos",
                                              "requestId": "4ad5f061-2849-4a7b-d832-37c8d9e0f1a2"
                                            }
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
                                              "timestamp": "2026-06-20T19:01:35.000",
                                              "path": "/pedidos",
                                              "requestId": "5be60172-3950-4b8c-e943-48d9e0f1a2b3"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário autenticado não possui nenhuma das roles ADMIN, GERENTE, COZINHA ou ATENDENTE.",
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
                                              "timestamp": "2026-06-20T19:02:47.000",
                                              "path": "/pedidos",
                                              "requestId": "6cf71283-4a61-4c9d-fa54-59e0f1a2b3c4"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GERENTE', 'ROLE_COZINHA', 'ROLE_ATENDENTE')")
    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorCanal(@RequestParam CanalPedido canalPedido){
        var pedidos = pedidoService.listarPedidosPorCanal(canalPedido);
        return ResponseEntity.ok().body(pedidos.stream().map(p -> new PedidoResponse(p)).toList());
    }


}
