package com.luc.raizesdeserto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luc.raizesdeserto.config.JWTUserData;
import com.luc.raizesdeserto.config.JacksonConfig;
import com.luc.raizesdeserto.config.SecurityConfig;
import com.luc.raizesdeserto.config.TokenConfig;
import com.luc.raizesdeserto.controller.PedidoController;
import com.luc.raizesdeserto.domain.entity.Pedido;
import com.luc.raizesdeserto.domain.entity.Unidade;
import com.luc.raizesdeserto.domain.entity.Usuario;
import com.luc.raizesdeserto.domain.enums.CanalPedido;
import com.luc.raizesdeserto.domain.enums.FormaPagamento;
import com.luc.raizesdeserto.domain.enums.Role;
import com.luc.raizesdeserto.domain.enums.Status;
import com.luc.raizesdeserto.dto.pedido.AtualizarStatusRequest;
import com.luc.raizesdeserto.dto.pedido.CriarPedidoRequest;
import com.luc.raizesdeserto.dto.pedido.ItemPedidoRequest;
import com.luc.raizesdeserto.infra.GatewayPagamentoClient;
import com.luc.raizesdeserto.service.PagamentoService;
import com.luc.raizesdeserto.service.PedidoService;
import com.luc.raizesdeserto.service.UnidadeService;
import com.luc.raizesdeserto.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@DisplayName("PedidoController - Testes de integração")
@AutoConfigureMockMvc
// Necessário: @WebMvcTest não carrega @Configuration "comuns" como SecurityConfig
// (que tem @EnableMethodSecurity, responsável por habilitar o @PreAuthorize) e
// JacksonConfig (que expõe o ObjectMapper real da aplicação).
@Import({SecurityConfig.class, JacksonConfig.class})
class PedidoControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PedidoService pedidoService;

    @MockitoBean
    UnidadeService unidadeService;

    @MockitoBean
    UsuarioService usuarioService;

    @MockitoBean
    GatewayPagamentoClient gatewayPagamentoClient;

    @MockitoBean
    PagamentoService pagamentoService;

    // Necessário: SecurityFilter é detectado pelo @WebMvcTest (implementa Filter)
    // e seu construtor exige TokenConfig.
    @MockitoBean
    TokenConfig tokenConfig;

    private UUID unidadeId;
    private UUID produtoId;
    private UUID usuarioId;
    private Unidade unidade;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        unidadeId = UUID.randomUUID();
        produtoId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();

        unidade = new Unidade();
        unidade.setId(unidadeId);

        usuario = new Usuario();
        usuario.setId(usuarioId);
    }

    @Test
    @WithMockUser(roles = "COZINHA")
    void deveRetornar403_QuandoCozinhaTentaCriarPedido() throws Exception {
        CriarPedidoRequest request = CriarPedidoRequest.builder()
                .unidadeId(unidadeId)
                .canalPedido(CanalPedido.APP)
                .itens(List.of(
                        ItemPedidoRequest.builder()
                                .produtoId(produtoId)
                                .quantidade(1)
                                .build()
                ))
                .formaPagamento(FormaPagamento.PIX)
                .build();

        mockMvc.perform(post("/pedidos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar200_QuandoClienteAutenticadoCriaPedido() throws Exception {
        // Arrange
        CriarPedidoRequest request = CriarPedidoRequest.builder()
                .unidadeId(unidadeId)
                .canalPedido(CanalPedido.APP)
                .itens(List.of(
                        ItemPedidoRequest.builder()
                                .produtoId(produtoId)
                                .quantidade(2)
                                .build()
                ))
                .formaPagamento(FormaPagamento.PIX)
                .build();

        Pedido pedidoSalvo = new Pedido();
        pedidoSalvo.setId(UUID.randomUUID());
        pedidoSalvo.setStatus(Status.AGUARDANDO_PAGAMENTO);
        pedidoSalvo.setCanalPedido(CanalPedido.APP);
        pedidoSalvo.setValorTotal(new BigDecimal("30.00"));

        JWTUserData jwtUser = JWTUserData.builder()
                .id(usuarioId)
                .email("cliente@email.com")
                .role(Role.ROLE_CLIENTE)
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        jwtUser,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                );

        when(unidadeService.buscarPorId(unidadeId))
                .thenReturn(unidade);
        when(usuarioService.buscarPorId(usuarioId))
                .thenReturn(Optional.of(usuario));
        when(pedidoService.criarPedido(any(Pedido.class)))
                .thenReturn(pedidoSalvo);
        when(gatewayPagamentoClient.enviarSolicitacaoPagamentoMock(any(Pedido.class)))
                .thenReturn("https://pagamento-mock.com.br/qr/abc123");

        // Act + Assert
        mockMvc.perform(post("/pedidos")
                        .with(authentication(authentication))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("AGUARDANDO_PAGAMENTO"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void deveRetornar400_QuandoListaItensVazia() throws Exception {
        String jsonInvalido = """
            {
              "unidadeId": "%s",
              "canalPedido": "APP",
              "itens": [],
              "formaPagamento": "PIX"
            }
            """.formatted(unidadeId);

        mockMvc.perform(post("/pedidos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "COZINHA")
    void deveRetornar200_QuandoAtualizaStatus() throws Exception {
        UUID pedidoId = UUID.randomUUID();
        AtualizarStatusRequest request = AtualizarStatusRequest.builder()
                .status(Status.PRONTO)
                .observacao("Pedido finalizado")
                .build();

        Pedido pedidoAtualizado = new Pedido();
        pedidoAtualizado.setId(pedidoId);
        pedidoAtualizado.setStatus(Status.PRONTO);

        when(pedidoService.atualizarStatus(
                any(),
                any(UUID.class),
                any(Status.class),
                any()))
                .thenReturn(pedidoAtualizado);

        mockMvc.perform(patch("/pedidos/{id}/status", pedidoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("PRONTO"));
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void deveRetornar404_QuandoPedidoNaoExiste() throws Exception {
        UUID pedidoId = UUID.randomUUID();
        AtualizarStatusRequest request = AtualizarStatusRequest.builder()
                .status(Status.CANCELADO)
                .observacao("Cliente desistiu")
                .build();

        when(pedidoService.atualizarStatus(
                any(),
                any(UUID.class),
                any(Status.class),
                any()))
                .thenThrow(new EntityNotFoundException(
                        "Pedido não encontrado"));

        mockMvc.perform(patch("/pedidos/{id}/status", pedidoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}