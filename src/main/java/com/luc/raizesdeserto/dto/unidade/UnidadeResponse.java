package com.luc.raizesdeserto.dto.unidade;

import com.luc.raizesdeserto.domain.entity.Unidade;
import com.luc.raizesdeserto.domain.enums.TipoOperacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalTime;
import java.util.UUID;

@Builder
public record UnidadeResponse(
        UUID id,
        Boolean ativa,
        String nome,
        String endereco,
        TipoOperacao tipoOperacao,
        LocalTime horarioAbertura,
        LocalTime horarioFechamento){

    public UnidadeResponse(Unidade u){
        this(u.getId(), u.getAtiva(), u.getNome(), u.getEndereco(), u.getTipoOperacao(), u.getHorarioAbertura(), u.getHorarioFechamento());
    }
}
