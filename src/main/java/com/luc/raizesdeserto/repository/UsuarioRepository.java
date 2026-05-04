package com.luc.raizesdeserto.repository;

import com.luc.raizesdeserto.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {


    Optional<Usuario> findUsuarioByEmail(String email);
}
