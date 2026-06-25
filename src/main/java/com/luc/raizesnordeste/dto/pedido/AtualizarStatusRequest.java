package com.luc.raizesnordeste.dto.pedido;

import com.luc.raizesnordeste.domain.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record  AtualizarStatusRequest(

        @NotNull(message = "Status é obrigatório.")
        Status status,
        String observacao
) {
}
