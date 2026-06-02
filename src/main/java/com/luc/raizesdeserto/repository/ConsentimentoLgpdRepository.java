package com.luc.raizesdeserto.repository;

import com.luc.raizesdeserto.domain.entity.ConsentimentoLGPD;
import com.luc.raizesdeserto.domain.entity.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConsentimentoLgpdRepository extends JpaRepository<ConsentimentoLGPD, UUID> {

}

