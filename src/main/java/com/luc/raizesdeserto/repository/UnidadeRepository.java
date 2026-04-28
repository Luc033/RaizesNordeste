package com.luc.raizesdeserto.repository;

import com.luc.raizesdeserto.domain.entity.Unidade;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnidadeRepository extends JpaRepository<Unidade, UUID> {
}
