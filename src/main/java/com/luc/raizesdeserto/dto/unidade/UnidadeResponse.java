package com.luc.raizesdeserto.dto.unidade;

import com.luc.raizesdeserto.domain.enums.TipoOperacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.UUID;

public record UnidadeResponse(
        UUID id,
        Boolean ativo,
        String nome,
        String endereco,
        TipoOperacao tipoOperacao,
        LocalTime horarioAbertura,
        LocalTime horarioFechamento){
}
