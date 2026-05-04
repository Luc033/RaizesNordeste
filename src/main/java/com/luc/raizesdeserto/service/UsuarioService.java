package com.luc.raizesdeserto.service;

import com.luc.raizesdeserto.domain.entity.Usuario;
import com.luc.raizesdeserto.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void salvar(Usuario usuario) {
        if(usuario.getId() == null){
            Optional<Usuario> userExists = usuarioRepository.findUsuarioByEmail(usuario.getEmail());

            if(userExists.isPresent()){
                //a  API retorna 409 Conflict
                throw new DataIntegrityViolationException("Email já existe: " + usuario.getEmail());
            }

            usuarioRepository.save(usuario);
        }else{
            Optional<Usuario> userExists = usuarioRepository.findById(usuario.getId());
            if(userExists.isPresent()){
                usuarioRepository.save(usuario);
            }else{
                // a API retorna 404 Not Found
                throw new EntityNotFoundException("Usuário não encontrado: " + usuario);
            }
        }

    }
}
