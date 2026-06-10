package com.luc.raizesdeserto;

import com.luc.raizesdeserto.config.TokenConfig;
import com.luc.raizesdeserto.controller.AuthController;
import com.luc.raizesdeserto.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

        // Json que será incluído no body da requisição contendo os dados do novo usuário (cliente)
        String jsonRequest = """
                {
                  "nome": "Cliente Teste",
                  "email": "cliente@email.com",
                  "senha": "senhaSegura123",
                  "aceitouTermos": false,
                  "role": "ROLE_CLIENTE"
                }
                """;
        // envia os dados do novo usuário via método POST
        mockMvc.perform(post("/auth/registrar-cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                // Espera que seja retornado Bad Request (400)
                .andExpect(status().isBadRequest())

                // Espera que a key error do response tenha 'Bad Request'
                .andExpect(jsonPath("$.error").value("Bad Request"))

                // Espera messagem padrão de erro
                .andExpect(jsonPath("$.message").value("Erro na validação dos campos da requisição."))

                // Espera que o campo preenchido incorretamente seja a key aceitouTermos
                .andExpect(jsonPath("$.details[0].field").value("aceitouTermos"))
                .andExpect(jsonPath("$.details[0].issue").exists());
    }
}