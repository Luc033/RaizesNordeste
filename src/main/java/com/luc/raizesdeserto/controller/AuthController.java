package com.luc.raizesdeserto.controller;

import com.luc.raizesdeserto.config.TokenConfig;
import com.luc.raizesdeserto.domain.entity.Usuario;
import com.luc.raizesdeserto.domain.enums.Role;
import com.luc.raizesdeserto.dto.auth.LoginRequest;
import com.luc.raizesdeserto.dto.auth.LoginResponse;
import com.luc.raizesdeserto.dto.usuario.RegisterRequest;
import com.luc.raizesdeserto.dto.usuario.RegisterResponse;
import com.luc.raizesdeserto.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    /// NEXT STEP:
    ///  * Implementar a classe AuthConfig antes de continuar.
    ///  * Implementar a classe TokenConfig,bem como, o seu método gerarToken e validarToken.
    ///  * Implementar a rota /register-cliente
    ///  * Implementar a rota /register-funcionario

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


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken userAndPass = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.senha());
        Authentication authentication = authenticationManager.authenticate(userAndPass);
        Usuario usuario = (Usuario) authentication.getPrincipal();
        String token = tokenConfig.gerarToken(usuario);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/registrar-cliente")
    public ResponseEntity registrarCliente(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {

        if(!registerRequest.aceitouTermos()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Necessário aceitar os termos de uso.");
        }

        Usuario novoUsuario = new Usuario();
        try {
            novoUsuario.setNome(registerRequest.nome());
            novoUsuario.setEmail(registerRequest.email());
            novoUsuario.setSenhaHash(passwordEncoder.encode(registerRequest.senha()));
            novoUsuario.setRole(Role.ROLE_CLIENTE);
            usuarioService.incluirConsentimento(novoUsuario.getId(), request);
            usuarioService.salvar(novoUsuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse(novoUsuario.getNome(), novoUsuario.getEmail()));
    }

    @PostMapping("/registrar-funcionario")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity registrarFuncionario(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        if(!registerRequest.aceitouTermos()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Necessário aceitar os termos de uso.");
        }

        Usuario novoUsuario = new Usuario();
        try {
            novoUsuario.setNome(registerRequest.nome());
            novoUsuario.setEmail(registerRequest.email());
            novoUsuario.setSenhaHash(passwordEncoder.encode(registerRequest.senha()));
            novoUsuario.setRole(registerRequest.role());
            usuarioService.salvar(novoUsuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse(novoUsuario.getNome(), novoUsuario.getEmail()));
    }

}
