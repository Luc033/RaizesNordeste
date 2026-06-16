package com.luc.raizesdeserto.dto.pedido;

import com.luc.raizesdeserto.domain.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record  AtualizarStatusRequest(

        @NotNull(message = "Status é obrigatório.")
        Status status,
        String observacao
) {
}
