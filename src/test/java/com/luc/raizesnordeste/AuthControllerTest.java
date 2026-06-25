package com.luc.raizesnordeste;

import com.luc.raizesnordeste.config.TokenConfig;
import com.luc.raizesnordeste.controller.AuthController;
import com.luc.raizesnordeste.domain.entity.Usuario;
import com.luc.raizesnordeste.domain.enums.Role;
import com.luc.raizesnordeste.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private TokenConfig tokenConfig;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Deve retornar 400 Bad Request com JSON padronizado se não aceitar os termos")
    void deveRetornarErroSeTermosNaoForemAceitos() throws Exception {

        // Json contendo os dados do novo usuário com aceitouTermos = false
        String jsonRequest = """
            {
              "nome": "Cliente Teste",
              "email": "cliente@email.com",
              "senha": "senhaSegura123",
              "aceitouTermos": false
            }
            """;

        // Envia os dados do novo usuário via método POST
        mockMvc.perform(post("/auth/registrar-cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                // Espera que seja retornado Bad Request (400)
                .andExpect(status().isBadRequest())

                // Espera que a key error do response tenha 'Bad Request'
                .andExpect(jsonPath("$.error").value("Bad Request"))

                // Espera mensagem padrão de erro
                .andExpect(jsonPath("$.message").value("Erro na validação dos campos da requisição."))

                // CORREÇÃO: Procura de forma dinâmica na lista pelo campo 'aceitouTermos'
                // O [?(@.field == 'aceitouTermos')] filtra a lista 'details' trazendo apenas o objeto correto
                .andExpect(jsonPath("$.details[?(@.field == 'aceitouTermos')].field").value("aceitouTermos"))
                .andExpect(jsonPath("$.details[?(@.field == 'aceitouTermos')].issue").value("Aceite de termos é obrigatório"));
    }

    @Test
    @DisplayName("Deve registrar um novo cliente com sucesso e retornar 201 Created com os dados do usuário")
    void deveRegistrarClienteComSucesso() throws Exception {

        // UUID simulado que o "banco" atribuiria ao usuário ao salvar
        UUID idGerado = UUID.randomUUID();

        // Como o controller monta o RegisterResponse a partir do próprio objeto
        // 'novoUsuario' (e não do retorno de salvar), simulamos o comportamento
        // do banco de dados setando o id diretamente na instância recebida.
        when(usuarioService.salvar(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioRecebido = invocation.getArgument(0);
            usuarioRecebido.setId(idGerado);
            return usuarioRecebido;
        });

        String jsonRequest = """
                {
                  "nome": "Cliente Teste",
                  "email": "cliente@email.com",
                  "senha": "senhaSegura123",
                  "aceitouTermos": true,
                  "role": "ROLE_CLIENTE"
                }
                """;

        mockMvc.perform(post("/auth/registrar-cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                // Espera que seja retornado Created (201)
                .andExpect(status().isCreated())

                // Espera que os dados do usuário criado sejam retornados corretamente
                .andExpect(jsonPath("$.id").value(idGerado.toString()))
                .andExpect(jsonPath("$.nome").value("Cliente Teste"))
                .andExpect(jsonPath("$.email").value("cliente@email.com"));

        // Garante que o consentimento LGPD foi registrado para o usuário recém-criado
        verify(usuarioService).incluirConsentimento(eq(idGerado), any());
    }

    @Test
    @DisplayName("Deve autenticar o usuário com sucesso e retornar o token JWT")
    void deveAutenticarComSucesso() throws Exception {

        // Usuário autenticado simulado, retornado como principal pelo AuthenticationManager
        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(UUID.randomUUID());
        usuarioAutenticado.setNome("Cliente Teste");
        usuarioAutenticado.setEmail("cliente@email.com");
        usuarioAutenticado.setSenhaHash("senhaHashSimulada");
        usuarioAutenticado.setRole(Role.ROLE_CLIENTE);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuarioAutenticado, null, usuarioAutenticado.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenConfig.gerarToken(usuarioAutenticado)).thenReturn("token-jwt-simulado");

        String jsonRequest = """
                {
                  "email": "cliente@email.com",
                  "senha": "senhaSegura123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                // Espera que seja retornado OK (200)
                .andExpect(status().isOk())

                // Espera que o token e o tempo de expiração sejam retornados corretamente
                .andExpect(jsonPath("$.token").value("token-jwt-simulado"))
                .andExpect(jsonPath("$.expiresIn").value(86400));
    }
}