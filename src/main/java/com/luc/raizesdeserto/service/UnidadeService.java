package com.luc.raizesdeserto.service;

import com.luc.raizesdeserto.domain.entity.Unidade;
import com.luc.raizesdeserto.repository.UnidadeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UnidadeService {
    private final UnidadeRepository unidadeRepository;


    public UnidadeService(UnidadeRepository unidadeRepository) {
        this.unidadeRepository = unidadeRepository;
    }

    public List<Unidade> listar(){
        return unidadeRepository.findAll();
    }

    public Unidade buscarPorId(UUID id){
        return unidadeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Unidade não encontrada: " + id));
    }
}
