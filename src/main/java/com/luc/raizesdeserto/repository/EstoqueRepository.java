package com.luc.raizesdeserto.repository;

import com.luc.raizesdeserto.domain.entity.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EstoqueRepository   extends JpaRepository<Estoque, UUID> {
}
