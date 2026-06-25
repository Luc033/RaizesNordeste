package com.luc.raizesnordeste.repository;

import com.luc.raizesnordeste.domain.entity.Unidade;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnidadeRepository extends JpaRepository<Unidade, UUID> {

}
