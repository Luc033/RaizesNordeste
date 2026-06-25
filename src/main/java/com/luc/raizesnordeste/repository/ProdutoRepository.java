package com.luc.raizesnordeste.repository;

import com.luc.raizesnordeste.domain.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    Optional<Produto> findProdutoByIdAndAtivoTrue(UUID  id);
    @Query("SELECT p FROM Produto p INNER JOIN Estoque e ON p.id = e.produto.id " +
    "WHERE e.unidade.id = :unidadeId")
    List<Produto> findAllByUnidadeId(@Param("unidadeId") UUID unidadeId);
}
