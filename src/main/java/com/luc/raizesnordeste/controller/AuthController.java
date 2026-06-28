package com.luc.raizesnordeste.controller;

import com.luc.raizesnordeste.config.TokenConfig;
import com.luc.raizesnordeste.domain.entity.Usuario;
import com.luc.raizesnordeste.domain.enums.Role;
import com.luc.raizesnordeste.dto.auth.LoginRequest;
import com.luc.raizesnordeste.dto.auth.LoginResponse;
import com.luc.raizesnordeste.dto.error.ErrorResponse;
import com.luc.raizesnordeste.dto.usuario.RegisterRequest;
import com.luc.raizesnordeste.dto.usuario.RegisterResponse;
import com.luc.raizesnordeste.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final TokenConfig tokenConfig;

    public AuthController(AuthenticationManager authenticationManager, UsuarioService usuarioService, PasswordEncoder passwordEncoder, TokenConfig tokenConfig) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.tokenConfig = tokenConfig;
    }


    @Operation(
            summary = "Autentica um usuário e gera um token de acesso",
            description = "Recebe email e senha via LoginRequest, autentica as credenciais através do AuthenticationManager/UsernamePasswordAuthenticationToken e, caso válidas, gera e retorna um token JWT (TokenConfig) com tempo de expiração para uso nas demais requisições autenticadas da API."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso. Token JWT e tempo de expiração (em segundos) retornados.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "LoginSucesso",
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c3VhcmlvSWQiOiJmM2ExYzJlNC04YjdkLTRhMmYtOWMzZS0xZDViNmE3ZjhlOWQiLCJyb2xlIjoiUk9MRV9DTElFTlRFIiwic3ViIjoiY2xpZW50ZUBlbWFpbC5jb20iLCJpYXQiOjE3NTAxNzAwMDAsImV4cCI6MTc1MDI1NjQwMH0.eXBmcGxlX2Fzc2luYXR1cmFfand0X3NpbXVsYWRh",
                                              "expiresIn": 86400
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida - campos obrigatórios (email/senha) ausentes ou em branco.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CamposObrigatoriosAusentes",
                                    value = """
                                            {
                                              "error": "Bad Request",
                                              "message": "Erro na validação dos campos da requisição.",
                                              "details": [
                                                {
                                                  "field": "email",
                                                  "issue": "Email é obrigatório"
                                                },
                                                {
                                                  "field": "senha",
                                                  "issue": "Senha é obrigatório"
                                                }
                                              ],
                                              "timestamp": "2026-06-20T17:30:00.000",
                                              "path": "/auth/login",
                                              "requestId": "a4e1f3c2-6b8d-4d2a-9f3e-7c5b6a8f1e2d"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas - email não cadastrado ou senha incorreta.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CredenciaisInvalidas",
                                    value = """
                                            {
                                              "error": "Unauthorized",
                                              "message": "Email ou senha inválidos.",
                                              "details": [],
                                              "timestamp": "2026-06-20T17:31:12.000",
                                              "path": "/auth/login",
                                              "requestId": "f3a1c2e4-8b7d-4a2f-9c3e-1d5b6a7f8e9d"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken userAndPass = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.senha());
        Authentication authentication = authenticationManager.authenticate(userAndPass);
        Usuario usuario = (Usuario) authentication.getPrincipal();
        String token = tokenConfig.gerarToken(usuario);
        Long expiresIn = 86400L;

        return ResponseEntity.ok(new LoginResponse(token, expiresIn));
    }



    @Operation(
            summary = "Registra um novo usuário com a role CLIENTE",
            description = "Cria uma nova conta de usuário do tipo cliente a partir dos dados informados em RegisterRequest, criptografa a senha (PasswordEncoder), registra o consentimento LGPD a partir dos dados da requisição HTTP (via UsuarioService) e retorna os dados básicos do usuário criado. Endpoint de acesso público."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário cliente registrado com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterResponse.class),
                            examples = @ExampleObject(
                                    name = "NovoUsuarioCliente",
                                    value = """
                                            {
                                              "id": "9b2e7f31-1a4c-4d6e-8f0a-2c3d4e5f6a7b",
                                              "nome": "Maria da Silva",
                                              "email": "maria.silva@email.com"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida - campos obrigatórios ausentes, mal formatados ou termo de uso não aceito.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ValidacaoFalhou",
                                    value = """
                                            {
                                              "error": "Bad Request",
                                              "message": "Erro na validação dos campos da requisição.",
                                              "details": [
                                                {
                                                  "field": "senha",
                                                  "issue": "Senha deve estar entre 8 e 50 caracteres."
                                                },
                                                {
                                                  "field": "aceitouTermos",
                                                  "issue": "Aceite de termos é obrigatório"
                                                },
                                                {
                                                  "field": "email",
                                                  "issue": "Email deve estar bem formado."
                                                }
                                              ],
                                              "timestamp": "2026-06-20T17:33:42.000",
                                              "path": "/auth/registrar-cliente",
                                              "requestId": "5c6d7e8f-2a3b-4c5d-9e0f-1a2b3c4d5e6f"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Já existe um usuário cadastrado com o e-mail informado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "EmailJaCadastrado",
                                    value = """
                                            {
                                              "error": "Conflict",
                                              "message": "Email já existe: maria.silva@email.com",
                                              "details": [],
                                              "timestamp": "2026-06-20T17:34:55.000",
                                              "path": "/auth/registrar-cliente",
                                              "requestId": "9b2e7f31-1a4c-4d6e-8f0a-2c3d4e5f6a7b"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/registrar-cliente")
    public ResponseEntity<RegisterResponse> registrarCliente(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(registerRequest.nome().trim());
        novoUsuario.setEmail(registerRequest.email());
        novoUsuario.setSenhaHash(passwordEncoder.encode(registerRequest.senha()));
        novoUsuario.setRole(Role.ROLE_CLIENTE);
        var usuarioSalvo = usuarioService.salvar(novoUsuario);
        usuarioService.incluirConsentimento(usuarioSalvo.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse(novoUsuario));
    }



    @Operation(
            summary = "Registra um novo usuário funcionário",
            description = "Cria um novo usuário com a role informada na requisição (RegisterRequest), criptografa a senha (PasswordEncoder) e registra o consentimento LGPD a partir dos dados da requisição HTTP. Acesso restrito a usuários autenticados com role ADMIN (@PreAuthorize)."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados para cadastro do funcionário.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples = @ExampleObject(
                            name = "Exemplo de requisição",
                            value = """
            {
              "nome": "João da Silva",
              "email": "joao.silva@empresa.com",
              "senha": "Senha@123",
              "role": "ROLE_ATENDENTE"
            }
            """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário funcionário registrado com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterResponse.class),
                            examples = @ExampleObject(
                                    name = "NovoUsuarioFuncionario",
                                    value = """
                                            {
                                              "id": "1d4e6f72-3b5c-4a8d-9e0f-5a6b7c8d9e0f",
                                              "nome": "João Pereira",
                                              "email": "joao.pereira@raizesnordeste.com"
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
                                    name = "ValidacaoFalhou",
                                    value = """
                                            {
                                              "error": "Bad Request",
                                              "message": "Erro na validação dos campos da requisição.",
                                              "details": [
                                                {
                                                  "field": "nome",
                                                  "issue": "Nome é obrigatório."
                                                },
                                                {
                                                  "field": "role",
                                                  "issue": "must not be null"
                                                }
                                              ],
                                              "timestamp": "2026-06-20T17:36:08.000",
                                              "path": "/auth/registrar-funcionario",
                                              "requestId": "3e4f5a6b-7c8d-49e0-9f1a-2b3c4d5e6f70"
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
                                              "timestamp": "2026-06-20T17:37:21.000",
                                              "path": "/auth/registrar-funcionario",
                                              "requestId": "6a7b8c9d-0e1f-4a2b-8c3d-4e5f6a7b8c9d"
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
                                              "timestamp": "2026-06-20T17:38:47.000",
                                              "path": "/auth/registrar-funcionario",
                                              "requestId": "7b8c9d0e-1f2a-4b3c-9d4e-5f6a7b8c9d0e"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Já existe um usuário cadastrado com o e-mail informado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "EmailJaCadastrado",
                                    value = """
                                            {
                                              "error": "Conflict",
                                              "message": "Email já existe: joao.pereira@raizesnordeste.com",
                                              "details": [],
                                              "timestamp": "2026-06-20T17:39:59.000",
                                              "path": "/auth/registrar-funcionario",
                                              "requestId": "1d4e6f72-3b5c-4a8d-9e0f-5a6b7c8d9e0f"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/registrar-funcionario")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<RegisterResponse> registrarFuncionario(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(registerRequest.nome().trim());
        novoUsuario.setEmail(registerRequest.email());
        novoUsuario.setSenhaHash(passwordEncoder.encode(registerRequest.senha()));
        novoUsuario.setRole(registerRequest.role());
        var usuarioSalvo = usuarioService.salvar(novoUsuario);
        usuarioService.incluirConsentimento(usuarioSalvo.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse(novoUsuario));
    }
}
