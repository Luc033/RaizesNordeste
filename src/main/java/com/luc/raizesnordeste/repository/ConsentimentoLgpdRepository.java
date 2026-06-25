package com.luc.raizesnordeste.repository;

import com.luc.raizesnordeste.domain.entity.ConsentimentoLGPD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConsentimentoLgpdRepository extends JpaRepository<ConsentimentoLGPD, UUID> {

}

