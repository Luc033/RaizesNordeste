package com.luc.raizesnordeste.controller;

import com.luc.raizesnordeste.domain.entity.Produto;
import com.luc.raizesnordeste.dto.error.ErrorResponse;
import com.luc.raizesnordeste.dto.produto.ProdutoRequest;
import com.luc.raizesnordeste.dto.produto.ProdutoResponse;
import com.luc.raizesnordeste.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController()
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Operation(
            summary = "Lista todos os produtos",
            description = "Retorna a lista completa de produtos cadastrados no sistema, independentemente da unidade ou status de ativação. Endpoint de acesso público."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produtos listados com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = ProdutoResponse.class)),
                            examples = @ExampleObject(
                                    name = "ProdutosListadosComSucesso",
                                    value = """
                                            [
                                              {
                                                "id": "3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                                "ativo": true,
                                                "nome": "Hambúrguer Artesanal",
                                                "descricao": "Pão brioche, blend 180g, queijo cheddar e bacon.",
                                                "precoBase": 32.50,
                                                "categoria": "Lanches",
                                                "sazonal": false
                                              },
                                              {
                                                "id": "7f3e1a2b-5c6d-4b9e-8f1a-9c0d1e2f3a4b",
                                                "ativo": true,
                                                "nome": "Refrigerante Lata",
                                                "descricao": "Lata 350ml.",
                                                "precoBase": 8.00,
                                                "categoria": "Bebidas",
                                                "sazonal": false
                                              }
                                            ]
                                            """
                            )
                    )
            )
    })
    @GetMapping("/produtos")
    public ResponseEntity<List<ProdutoResponse>> listarTodosProdutos() {
        var produtos = produtoService.listar().stream().map(p -> new ProdutoResponse(p)).toList();
        return ResponseEntity.ok().body(produtos);
    }

    @Operation(
            summary = "Lista os produtos disponíveis em uma unidade",
            description = "Retorna a lista de produtos vinculados ao estoque da unidade informada via path variable 'unidadeId'. Endpoint de acesso público."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produtos da unidade listados com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = ProdutoResponse.class)),
                            examples = @ExampleObject(
                                    name = "ProdutosDaUnidadeListadosComSucesso",
                                    value = """
                                            [
                                              {
                                                "id": "3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                                "ativo": true,
                                                "nome": "Hambúrguer Artesanal",
                                                "descricao": "Pão brioche, blend 180g, queijo cheddar e bacon.",
                                                "precoBase": 32.50,
                                                "categoria": "Lanches",
                                                "sazonal": false
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhuma unidade foi encontrada para o ID informado no path.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UnidadeNaoEncontrada",
                                    value = """
                                            {
                                              "error": "Not Found",
                                              "message": "Unidade não encontrada: d6bc413c-efb8-4786-9247-94259ae38cc1",
                                              "details": [],
                                              "timestamp": "2026-06-20T19:10:14.000",
                                              "path": "/unidades/d6bc413c-efb8-4786-9247-94259ae38cc1/produtos",
                                              "requestId": "1b4c6d72-3e50-4071-8fa3-4d5e60718293"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/unidades/{unidadeId}/produtos")
    public ResponseEntity<List<ProdutoResponse>> listarTodosProdutosPorUnidade(@PathVariable UUID unidadeId) {
        var produtos = produtoService.listarProdutosPorUnidade(unidadeId).stream().map(p -> new ProdutoResponse(p)).toList();
        return ResponseEntity.ok().body(produtos);
    }

    @Operation(
            summary = "Busca um produto pelo identificador",
            description = "Retorna os dados completos de um produto cadastrado (nome, descrição, preço base, categoria, sazonalidade e status de ativação) a partir do seu ID. Endpoint de acesso público."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto encontrado com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProdutoResponse.class),
                            examples = @ExampleObject(
                                    name = "ProdutoEncontrado",
                                    value = """
                                            {
                                              "id": "3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "ativo": true,
                                              "nome": "Hambúrguer Artesanal",
                                              "descricao": "Pão brioche, blend 180g, queijo cheddar e bacon.",
                                              "precoBase": 32.50,
                                              "categoria": "Lanches",
                                              "sazonal": false
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado para o ID informado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ProdutoNaoEncontrado",
                                    value = """
                                            {
                                              "error": "Not Found",
                                              "message": "Produto não encontrado: 3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "details": [],
                                              "timestamp": "2026-06-20T19:11:25.000",
                                              "path": "/produtos/3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "requestId": "2c5d7e83-4f61-4182-90b4-5e60718293a4"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/produtos/{id}")
    public ResponseEntity<ProdutoResponse> buscarProduto(@PathVariable UUID id) {
        return ResponseEntity.ok().body(new ProdutoResponse(produtoService.buscarPorId(id)));
    }

    @Operation(
            summary = "Cria um novo produto",
            description = "Cadastra um novo produto com nome, descrição, preço base, categoria e indicação de sazonalidade (ProdutoRequest). Acesso restrito a ADMIN e GERENTE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Produto criado com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProdutoResponse.class),
                            examples = @ExampleObject(
                                    name = "ProdutoCriadoComSucesso",
                                    value = """
                                            {
                                              "id": "9d6c8a3f-2b1e-4f7a-8c9d-0e1f2a3b4c5d",
                                              "ativo": true,
                                              "nome": "Suco Natural de Laranja",
                                              "descricao": "Suco natural, 500ml, sem adição de açúcar.",
                                              "precoBase": 12.00,
                                              "categoria": "Bebidas",
                                              "sazonal": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação nos campos da requisição de criação do produto.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ValidacaoProduto",
                                    value = """
                                            {
                                              "error": "Bad Request",
                                              "message": "Erro na validação dos campos da requisição.",
                                              "details": [
                                                {
                                                  "field": "nome",
                                                  "issue": "Nome de produto é obrigatório."
                                                },
                                                {
                                                  "field": "precoBase",
                                                  "issue": "Preço de produto base é obrigatório."
                                                }
                                              ],
                                              "timestamp": "2026-06-20T19:12:36.000",
                                              "path": "/produtos",
                                              "requestId": "3d6e8f94-5072-4293-a1c5-6f718293a4b5"
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
                                              "timestamp": "2026-06-20T19:13:48.000",
                                              "path": "/produtos",
                                              "requestId": "4e7f9a05-6183-4304-b2d6-718293a4b5c6"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário autenticado não possui as roles ADMIN ou GERENTE exigidas.",
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
                                              "timestamp": "2026-06-20T19:14:59.000",
                                              "path": "/produtos",
                                              "requestId": "5f80ab16-7294-4415-c3e7-8293a4b5c6d7"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/produtos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ProdutoResponse> criarProduto(
            @Valid @RequestBody ProdutoRequest request) {

        Produto novoProduto = new Produto();
        novoProduto.setNome(request.nome().trim());
        novoProduto.setDescricao(request.descricao());
        novoProduto.setPrecoBase(request.precoBase());
        novoProduto.setCategoria(request.categoria());
        novoProduto.setSazonal(request.sazonal());

        Produto produtoSalvo = produtoService.salvar(novoProduto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProdutoResponse(produtoSalvo));
    }

    @Operation(
            summary = "Atualiza um produto existente",
            description = "Atualiza nome, descrição, preço base, categoria e sazonalidade de um produto já cadastrado (ProdutoRequest), identificado pelo seu ID. Acesso restrito a ADMIN e GERENTE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto atualizado com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProdutoResponse.class),
                            examples = @ExampleObject(
                                    name = "ProdutoAtualizadoComSucesso",
                                    value = """
                                            {
                                              "id": "3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "ativo": true,
                                              "nome": "Hambúrguer Artesanal Premium",
                                              "descricao": "Pão brioche, blend 180g, queijo cheddar, bacon e cebola caramelizada.",
                                              "precoBase": 36.90,
                                              "categoria": "Lanches",
                                              "sazonal": false
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação nos campos da requisição de atualização do produto.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ValidacaoProduto",
                                    value = """
                                            {
                                              "error": "Bad Request",
                                              "message": "Erro na validação dos campos da requisição.",
                                              "details": [
                                                {
                                                  "field": "precoBase",
                                                  "issue": "Preço de produto base deve ser igual ou maior que zero."
                                                }
                                              ],
                                              "timestamp": "2026-06-20T19:16:11.000",
                                              "path": "/produtos/3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "requestId": "6091bc27-8305-4526-d4f8-93a4b5c6d7e8"
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
                                              "timestamp": "2026-06-20T19:17:23.000",
                                              "path": "/produtos/3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "requestId": "71a2cd38-9416-4637-e509-a4b5c6d7e8f9"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário autenticado não possui as roles ADMIN ou GERENTE exigidas.",
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
                                              "timestamp": "2026-06-20T19:18:34.000",
                                              "path": "/produtos/3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "requestId": "82b3de49-a527-4748-f610-b5c6d7e8f9a0"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado para o ID informado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ProdutoNaoEncontrado",
                                    value = """
                                            {
                                              "error": "Not Found",
                                              "message": "Produto não encontrado com o ID: 3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "details": [],
                                              "timestamp": "2026-06-20T19:19:46.000",
                                              "path": "/produtos/3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "requestId": "93c4ef50-b638-4859-a721-c6d7e8f9a0b1"
                                            }
                                            """
                            )
                    )
            )
    })
    @PutMapping("/produtos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ProdutoResponse> atualizarProduto(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoRequest request) {

        Produto produtoAtualizado = produtoService.atualizar(id, request);
        return ResponseEntity.ok(new ProdutoResponse(produtoAtualizado));
    }

    @Operation(
            summary = "Remove um produto",
            description = "Exclui definitivamente um produto cadastrado a partir do seu ID. Acesso restrito a ADMIN e GERENTE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Produto removido com sucesso. A resposta não possui corpo."
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
                                              "path": "/produtos/3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "requestId": "a4d5f061-c749-4960-b832-d7e8f9a0b1c2"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - usuário autenticado não possui as roles ADMIN ou GERENTE exigidas.",
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
                                              "timestamp": "2026-06-20T19:22:09.000",
                                              "path": "/produtos/3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "requestId": "b5e60172-d850-4a71-c943-e8f9a0b1c2d3"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado para o ID informado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ProdutoNaoEncontrado",
                                    value = """
                                            {
                                              "error": "Not Found",
                                              "message": "Produto não encontrado com o ID: 3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "details": [],
                                              "timestamp": "2026-06-20T19:23:21.000",
                                              "path": "/produtos/3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "requestId": "c6f71283-e961-4b82-da54-f9a0b1c2d3e4"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "O produto não pode ser removido pois possui vínculos com outros registros do sistema (ex: itens de pedido ou estoque já lançado).",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ProdutoComVinculos",
                                    value = """
                                            {
                                              "error": "Conflict",
                                              "message": "Referência a um recurso inexistente.",
                                              "details": [],
                                              "timestamp": "2026-06-20T19:24:33.000",
                                              "path": "/produtos/3f2504e0-4f89-11d3-9a0c-0305e82c3301",
                                              "requestId": "d708239e-fa72-4c93-eb65-0a1b2c3d4e5f"
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/produtos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> deletarProduto(
            @PathVariable UUID id) {

        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}