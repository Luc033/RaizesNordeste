package com.luc.raizesdeserto.dto.error;

import lombok.Builder;

@Builder
public record ErroDetalhe(
     String field,
     String issue
) {
}
