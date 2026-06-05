package com.luc.raizesdeserto.infra;


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

    public GatewayPagamentoClient(@Value("${gateway.pagamento.mock.url-approved}") String urlApproved,
                                  @Value("${gateway.pagamento.mock.url-denied}") String urlDenied) {
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
     * @param payloadRequest corpo da requisição em formato esperado pelo gateway mock
     * @param simularErro    {@code true} para simular recusa do pagamento,
     *                       {@code false} para simular aprovação
     * @return resposta bruta do gateway mock como {@code String}
     * @throws RuntimeException se ocorrer falha na comunicação com o gateway
     */
    public String enviarSolicitacaoPagamentoMock(String payloadRequest, boolean simularErro){
        String url = simularErro ? urlDenied : urlApproved;

        try{
            return restClient.post()
                    .uri(url)
                    .body(payloadRequest)
                    .retrieve()
                    .body(String.class);
        }catch (Exception e){
            throw new RuntimeException("Falha ao comunicar com o gateway de pagamento");
        }
    }
}
