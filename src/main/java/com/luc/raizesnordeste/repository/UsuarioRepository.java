package com.luc.raizesnordeste.repository;

import com.luc.raizesnordeste.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {


    Optional<Usuario>  findUsuarioById(UUID id);

    Optional<Usuario>   findUsuarioByEmail(String email);

    Optional<Usuario> findUserByEmail(String email);
}
