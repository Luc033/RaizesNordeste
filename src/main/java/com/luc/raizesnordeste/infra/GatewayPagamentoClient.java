package com.luc.raizesnordeste.infra;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luc.raizesnordeste.domain.entity.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


/**
 * Cliente HTTP responsável pela integração com o gateway de pagamento mock.
 *
 * <p>Componente Spring que encapsula as chamadas ao serviço simulado de pagamento,
 * permitindo testar cenários de aprovação e recusa sem depender de uma integração real.
 * As URLs dos endpoints são configuradas via propriedades da aplicação.
 */
@Component
public class GatewayPagamentoClient {
    private final RestClient restClient;
    private String urlApproved;
    private String urlDenied;
    private final ObjectMapper objectMapper;

    public GatewayPagamentoClient(@Value("${gateway.pagamento.mock.url-approved}") String urlApproved,
                                  @Value("${gateway.pagamento.mock.url-denied}") String urlDenied, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
        this.urlApproved = urlApproved;
        this.urlDenied = urlDenied;
    }

    /**
     * Envia uma solicitação de pagamento simulada para o gateway mock, permitindo
     * testar cenários de aprovação e negação sem integração real.
     *
     * <p>Seleciona a URL de destino com base no parâmetro {@code simularErro}:
     * quando {@code true}, redireciona para o endpoint de negação; caso contrário,
     * para o endpoint de aprovação. Qualquer falha de comunicação é encapsulada
     * em uma {@link RuntimeException}.
     *
     * @param pedido pedido realizado
     * @return resposta bruta do gateway mock como {@code String}
     * @throws RuntimeException se ocorrer falha na comunicação com o gateway
     */
    public String enviarSolicitacaoPagamentoMock(Pedido pedido) {
        String payloadRequest = String.format(
                "{\"pedidoId\": \"%s\", \"valor\": %s}",
                pedido.getId().toString(),
                pedido.getValorTotal()
        );

        try {
            // Pega a resposta bruta
            String respostaJsonString = restClient.post()
                    .uri(this.urlApproved)
                    .header("Content-Type", "application/json")
                    .body(payloadRequest)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(respostaJsonString);

            // navega exatamente até o campo desejado
            String qrCodeExtraido = rootNode
                    .path("point_of_interaction")
                    .path("transaction_data")
                    .path("qr_code")
                    .asText();

            if (qrCodeExtraido.isEmpty() || qrCodeExtraido.equals("null")) {
                throw new RuntimeException("QR Code não encontrado na resposta do gateway.");
            }

            return qrCodeExtraido;

        } catch (Exception e) {
            throw new RuntimeException("Falha ao comunicar com o gateway de pagamento: " + e.getMessage());
        }
    }

}
