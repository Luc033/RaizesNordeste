package com.luc.raizesdeserto.dto.unidade;

import com.luc.raizesdeserto.domain.enums.TipoOperacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record UnidadeRequest(
        @Size(min = 1, max = 100, message = "Nome da unidade deve estar entre 1 e 100 caracteres.")
        @NotBlank(message = "Nome não pode ser nulo.")
        String nome,
        @Size(min = 1, max = 255, message = "Endereço da Unidade deve estar entre 1 e 255 caracteres.")
        @NotBlank(message = "Endereço não pode ser nulo.")
        String endereco,
        TipoOperacao tipoOperacao,

        @NotNull(message = "Horário de abertura da unidade não pode estar vazio.")
        LocalTime horarioAbertura,

        @NotNull(message = "Horário de fechamento da unidade não pode estar vazio.")
        LocalTime horarioFechamento) {
}
