package com.luc.raizesnordeste.service;

import com.luc.raizesnordeste.domain.entity.ConsentimentoLGPD;
import com.luc.raizesnordeste.domain.entity.Usuario;
import com.luc.raizesnordeste.repository.ConsentimentoLgpdRepository;
import com.luc.raizesnordeste.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ConsentimentoLgpdRepository lgpdRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, ConsentimentoLgpdRepository lgpdRepository) {
        this.usuarioRepository = usuarioRepository;
        this.lgpdRepository = lgpdRepository;
    }

    @Transactional
    public Usuario registrarNovoUsuario(Usuario usuario, HttpServletRequest request) {
        Usuario usuarioSalvo = this.salvar(usuario);
        this.incluirConsentimento(usuarioSalvo.getId(), request);
        return usuarioSalvo;
    }

    public Usuario salvar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }

        // Verifica se existe ID, se não existir, verifica se o email já existe
        if (usuario.getId() == null) {
            Optional<Usuario> userExists = this.buscarPorEmail(usuario.getEmail());

            if (userExists.isPresent()) {
                // A  API retorna 409 Conflict
                throw new DataIntegrityViolationException("Email já existe: " + usuario.getEmail());
            }

            return usuarioRepository.save(usuario);

            // Caso exista ID, verifica se o ID existe no Banco de Dados
        } else {
            Optional<Usuario> userExists = usuarioRepository.findById(usuario.getId());
            if (userExists.isPresent()) {
                return usuarioRepository.save(usuario);
            } else {
                // A API retorna 404 Not Found
                throw new EntityNotFoundException("Usuário não encontrado: " + usuario);
            }
        }

    }

    public Optional<Usuario> buscarPorEmail(String email) {
            return usuarioRepository.findUsuarioByEmail(email);
    }

    public Optional<Usuario> buscarPorId(UUID id) {
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findUsuarioById(id);
        if(usuarioEncontrado.isEmpty()){
            throw new EntityNotFoundException("Usuário não encontrado: " + id);
        }

        return usuarioEncontrado;
    }

    public void incluirConsentimento(UUID usuarioId, HttpServletRequest request) {
        ConsentimentoLGPD consentimento = new ConsentimentoLGPD();
        consentimento.setAceitou(true);
        consentimento.setFinalidade("Cadastro de conta, processamento de pedidos e realização de entregas.");
        String ipCliente = request.getHeader("X-Forwarded-For");
        if (ipCliente == null) {
            ipCliente = request.getRemoteAddr();
        }
        consentimento.setIpOrigem(ipCliente);
        Usuario usuarioRef = usuarioRepository.getReferenceById(usuarioId);
        consentimento.setUsuario(usuarioRef);
        lgpdRepository.save(consentimento);
    }


    public void excluirUsuario(UUID id) {
        var usuarioEncontrado = this.usuarioRepository.findById(id);
        if (usuarioEncontrado.isPresent()) {
            this.usuarioRepository.delete(usuarioEncontrado.get());
        }else {
            throw new EntityNotFoundException("Usuário não encontrado: "+id);
        }
    }
    @Transactional
    public Usuario atualizarDados(UUID id, String senhaHash) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));

        usuario.setSenhaHash(senhaHash);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void desativarUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

}
