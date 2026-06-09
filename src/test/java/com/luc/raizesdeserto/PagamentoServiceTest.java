package com.luc.raizesdeserto;

import com.luc.raizesdeserto.domain.entity.*;
import com.luc.raizesdeserto.domain.enums.* ;
import com.luc.raizesdeserto.repository.PagamentoRepository;
import com.luc.raizesdeserto.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    // Substituímos o Repository pelo Service real que a sua classe usa
    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private PagamentoService pagamentoService;

    @Test
    @DisplayName("Deve lançar IllegalArgumentException se o pagamento não estiver PENDENTE")
    void deveLancarExcecaoSePagamentoNaoEstiverPendente() {
        UUID pedidoId = UUID.randomUUID();

        // Cria um pagamento que já aprovado (vai falhar na regra do PENDENTE)
        Pagamento pagamentoAprovado = new Pagamento();
        pagamentoAprovado.setStatusPagamento(StatusPagamento.APROVADO);

        Pedido pedidoFake = new Pedido();
        pedidoFake.setId(pedidoId);
        pedidoFake.setPagamento(pagamentoAprovado);

        // Quando o PagamentoService pedir o pedido para o PedidoService, será devolvido o pedido fake
        when(pedidoService.buscarPorId(pedidoId)).thenReturn(pedidoFake);

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> pagamentoService.registrarRetorno(pedidoId, StatusPagamento.APROVADO, "Payload")
        );

        // Verifica se o metódo capturou o erro e retornou mensagem corretamente
        assertTrue(excecao.getMessage().contains("Não é possível registrar o retorno do pagamento"));
    }
}