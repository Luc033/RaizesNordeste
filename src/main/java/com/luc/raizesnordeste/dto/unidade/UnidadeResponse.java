package com.luc.raizesnordeste.dto.unidade;

import com.luc.raizesnordeste.domain.entity.Unidade;
import com.luc.raizesnordeste.domain.enums.TipoOperacao;
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
