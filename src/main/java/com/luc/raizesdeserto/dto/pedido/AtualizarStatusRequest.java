package com.luc.raizesdeserto.dto.pedido;

import com.luc.raizesdeserto.domain.enums.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AtualizarStatusRequest(

        @NotBlank(message = "Status é obrigatório.")
        Status status,
        String observacao
) {
}
