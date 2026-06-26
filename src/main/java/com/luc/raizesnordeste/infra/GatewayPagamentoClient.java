package com.luc.raizesnordeste.infra;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luc.raizesnordeste.domain.entity.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


/**
 * Integra a aplicação ao gateway de pagamento mock, enviando solicitações
 * de pagamento de pedidos e extraindo o QR Code retornado pela simulação.
 */
@Component
public class GatewayPagamentoClient {
    private final RestClient restClient;
    private String url;
    private final ObjectMapper objectMapper;

    public GatewayPagamentoClient(@Value("${gateway.pagamento.mock.url-approved}") String url,
                                 ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
        this.url = url;
    }


    /**
     * Simula a solicitação de pagamento de um pedido no gateway externo e extrai o QR Code
     * retornado para continuidade do fluxo de pagamento. Garante que a resposta do gateway
     * contenha um QR Code válido, impedindo que o pedido avance sem dados de pagamento.
     *
     * @param pedido pedido cujo identificador e valor total serão enviados ao gateway mock
     * @return QR Code de pagamento retornado pelo gateway
     * @throws RuntimeException quando o gateway não retorna um QR Code válido ou quando ocorre falha de comunicação/processamento da resposta
     */
    public String enviarSolicitacaoPagamentoMock(Pedido pedido) {
        String payloadRequest = String.format(
                "{\"pedidoId\": \"%s\", \"valor\": %s}",
                pedido.getId().toString(),
                pedido.getValorTotal()
        );

        try {
            String respostaJsonString = restClient.post()
                    .uri(this.url)
                    .header("Content-Type", "application/json")
                    .body(payloadRequest)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(respostaJsonString);

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
