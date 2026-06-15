package com.luc.raizesdeserto.dto.pagamento;

import com.luc.raizesdeserto.domain.enums.Status;
import com.luc.raizesdeserto.domain.enums.StatusPagamento;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record PagamentoMockRequest(
        @NotNull(message = "pedidoId é obrigatório.")
        UUID pedidoId,
        @NotNull(message = "statusPagamento é obrigatório")
        StatusPagamento statusPagamento,
        @NotNull(message = "payload é obrigatório")
        String payload
) {
}

/*

{
  "id": 987654321,
  "status": "APROVADO",
  "status_detail": "pending_waiting_transfer",
  "currency_id": "BRL",
  "transaction_amount": 76.50,
  "description": "Pedido #86c16d3c - Raízes do Deserto",
  "point_of_interaction": {
    "type": "PIX",
    "transaction_data": {
      "qr_code": "00020101021126580014br.gov.bcb.pix0136123e4567-e89b-12d3-a456-426614174000520400005303986540576.505802BR5917Raizes do Deserto6008Curitiba62070503***6304E4A2",
      "qr_code_base64": "iVBORw0KGgoAAAANSUhEUgAAAcIAAAHCMAAAABc/s1AAAABlBMVEX///8AAABVwtR+AAAAHklEQVR42u3BAQEAAACCIP+vbxdAAAAAAMBgzwAAIAB6QAAFAAEwHQAAAABJRU5ErkJggg==",
      "ticket_url": "https://www.mercadopago.com.br/receipt/987654321"
    }
  },
  "date_created": "2026-06-15T20:13:00.000-03:00",
  "date_of_expiration": "2026-06-15T20:43:00.000-03:00"
}

 */