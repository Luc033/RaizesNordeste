package com.luc.raizesnordeste.service;

import com.luc.raizesnordeste.domain.entity.Unidade;
import com.luc.raizesnordeste.repository.UnidadeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Gerencia a consulta das unidades da rede, representando os pontos
 * operacionais associados ao catálogo, estoque e pedidos.
 */
@Service
public class UnidadeService {
    private final UnidadeRepository unidadeRepository;


    public UnidadeService(UnidadeRepository unidadeRepository) {
        this.unidadeRepository = unidadeRepository;
    }

    /**
     * Lista todas as unidades cadastradas na base de dados.
     *
     * <p>Este método pertence à camada de Service e centraliza a consulta geral de unidades,
     * retornando todos os registros disponíveis sem aplicação de filtros.</p>
     *
     * @return lista com todas as unidades cadastradas.
     */
    public List<Unidade> listar(){
        return unidadeRepository.findAll();
    }

    /**
     * Busca uma unidade pelo seu identificador único.
     *
     * <p>Este método pertence à camada de Service e centraliza a recuperação de uma unidade,
     * lançando exceção caso o registro não exista na base de dados.</p>
     *
     * @param id identificador único da unidade.
     * @return unidade encontrada.
     * @throws EntityNotFoundException quando nenhuma unidade é encontrada para o identificador informado.
     */
    public Unidade buscarPorId(UUID id){
        return unidadeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Unidade não encontrada: " + id));
    }
}
