package com.luc.raizesnordeste.controller;

import com.luc.raizesnordeste.domain.entity.Usuario;
import com.luc.raizesnordeste.dto.error.ErrorResponse;
import com.luc.raizesnordeste.dto.usuario.AtualizarSenhaUsuarioRequest;
import com.luc.raizesnordeste.dto.usuario.RegisterResponse;
import com.luc.raizesnordeste.service.UsuarioService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController()
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(
            summary = "Busca os dados de registro de um usuário",
            description = "Retorna os dados de nome e email de um usuário previamente registrado, a partir do seu ID. Acesso restrito a ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterResponse.class),
                            examples = @ExampleObject(
                                    name = "UsuarioEncontrado",
                                    value = """
                                            {
                                              "id": "2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                              "nome": "Maria da Silva",
                                              "email": "maria.silva@email.com"
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
                                              "timestamp": "2026-06-20T19:30:12.000",
                                              "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                              "requestId": "e8f9a0b1-c2d3-4e4f-a506-1b2c3d4e5f60"
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
                                              "timestamp": "2026-06-20T19:31:24.000",
                                              "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                              "requestId": "f9a0b1c2-d3e4-4f50-b617-2c3d4e5f6071"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado para o ID informado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UsuarioNaoEncontrado",
                                    value = """
                                            {
                                              "error": "Not Found",
                                              "message": "Usuário não encontrado: 2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                              "details": [],
                                              "timestamp": "2026-06-20T19:35:01.000",
                                              "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                              "requestId": "2c3d4e5f-6071-4829-9b50-3d4e5f607182"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("{id}")
    public ResponseEntity<RegisterResponse> usuarioRegistrado(@RequestBody UUID id) {
        var usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(new RegisterResponse(usuario.get()));
    }


    @Operation(
            summary = "Exclui um usuário",
            description = "Remove definitivamente o usuário informado pelo ID. Acesso restrito a ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuário excluído com sucesso. A resposta não possui corpo."
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
                                              "timestamp": "2026-06-20T19:32:36.000",
                                              "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                              "requestId": "0a1b2c3d-4e5f-4061-c728-3d4e5f607182"
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
                                              "timestamp": "2026-06-20T19:33:48.000",
                                              "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                              "requestId": "1b2c3d4e-5f60-4172-d839-4e5f60718293"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado para o ID informado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UsuarioNaoEncontrado",
                                    value = """
                                            {
                                              "error": "Not Found",
                                              "message": "Usuário não encontrado: 2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                              "details": [],
                                              "timestamp": "2026-06-20T12:01:05.000",
                                              "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                              "requestId": "3d6e8f94-5b7c-4a0d-ae42-6a7b8c9d0e1f"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> usuarioExcluir(@PathVariable UUID id) {
        usuarioService.excluirUsuario(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }
    @Operation(
            summary = "Atualiza a senha de um usuário",
            description = "Atualiza a senha (criptografada) do usuário identificado pelo ID informado no path. Pode ser executado pelo próprio titular da conta, ou por usuários com role ADMIN ou GERENTE em nome de qualquer usuário."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Dados do usuário atualizados com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterResponse.class),
                            examples = @ExampleObject(
                                    name = "UsuarioAtualizadoComSucesso",
                                    value = """
                                        {
                                          "id": "2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                          "nome": "Maria da Silva Santos",
                                          "email": "maria.silva@email.com"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida - campos obrigatórios ausentes ou mal formatados.",
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
                                              "field": "senha",
                                              "issue": "Senha deve estar entre 8 e 50 caracteres."
                                            }
                                          ],
                                          "timestamp": "2026-06-21T14:10:22.000",
                                          "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                          "requestId": "4d5e6f70-8192-4a3b-9c0d-1e2f3a4b5c6d"
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
                                          "timestamp": "2026-06-21T14:11:34.000",
                                          "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                          "requestId": "5e6f7081-9203-4b4c-ad1e-2f3a4b5c6d7e"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - o usuário autenticado não é o titular da conta e não possui as roles ADMIN ou GERENTE.",
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
                                          "timestamp": "2026-06-21T14:12:46.000",
                                          "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                          "requestId": "6f708192-0314-4c5d-be2f-3a4b5c6d7e8f"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado para o ID informado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UsuarioNaoEncontrado",
                                    value = """
                                        {
                                          "error": "Not Found",
                                          "message": "Usuário não encontrado: 2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                          "details": [],
                                          "timestamp": "2026-06-21T14:13:58.000",
                                          "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                          "requestId": "70819203-1425-4d6e-cf30-4b5c6d7e8f90"
                                        }
                                        """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE') or #id == authentication.principal.id()")
    @PatchMapping("{id}")
    public ResponseEntity<RegisterResponse> atualizarSenhaUsuario(@PathVariable UUID id,
                                                             @Valid @RequestBody AtualizarSenhaUsuarioRequest request) {
        String senhaHash = passwordEncoder.encode(request.senha());
        Usuario usuarioAtualizado = usuarioService.atualizarDados(id, senhaHash);
        return ResponseEntity.ok(new RegisterResponse(usuarioAtualizado));
    }


    @Operation(
            summary = "Desativa um usuário",
            description = "Altera o atributo 'ativo' do usuário informado pelo ID para false, desativando sua conta sem excluí-la definitivamente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuário desativado com sucesso. A resposta não possui corpo."
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
                                          "timestamp": "2026-06-21T14:15:10.000",
                                          "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e/desativar",
                                          "requestId": "81920314-2536-4e7f-d041-5c6d7e8f90a1"
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
                                          "timestamp": "2026-06-21T14:16:22.000",
                                          "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e/desativar",
                                          "requestId": "92031425-3647-4f80-e152-6d7e8f90a1b2"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado para o ID informado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UsuarioNaoEncontrado",
                                    value = """
                                        {
                                          "error": "Not Found",
                                          "message": "Usuário não encontrado: 2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e",
                                          "details": [],
                                          "timestamp": "2026-06-21T14:17:34.000",
                                          "path": "/usuarios/2c5d7e83-4a6b-4f9c-9d31-5f6a7b8c9d0e/desativar",
                                          "requestId": "a3142536-4758-4091-f263-7e8f90a1b2c3"
                                        }
                                        """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE') or " +
            "(hasAuthority('ROLE_CLIENTE') and #id == authentication.principal.id())"
    )
    @PatchMapping("{id}/desativar")
    public ResponseEntity<Void> desativarUsuario(@PathVariable UUID id) {
        usuarioService.desativarUsuario(id);
        return ResponseEntity.noContent().build();
    }

}
