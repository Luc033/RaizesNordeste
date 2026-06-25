package com.luc.raizesnordeste.dto.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ErroDetalhe(
        @JsonProperty("field")
        String field,
        @JsonProperty("issue")
        String issue
) {
}
