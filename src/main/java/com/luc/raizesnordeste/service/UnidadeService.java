package com.luc.raizesnordeste.service;

import com.luc.raizesnordeste.domain.entity.Unidade;
import com.luc.raizesnordeste.repository.UnidadeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
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
