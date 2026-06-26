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

/**
 * Orquestra o cadastro e a gestão de usuários da aplicação, incluindo
 * validação de unicidade, atualização, desativação e registro de consentimento LGPD.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ConsentimentoLgpdRepository lgpdRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, ConsentimentoLgpdRepository lgpdRepository) {
        this.usuarioRepository = usuarioRepository;
        this.lgpdRepository = lgpdRepository;
    }

    /**
     * Registra um novo usuário e vincula o consentimento aos termos de uso.
     *
     * <p>Este método pertence à camada de Service e orquestra o cadastro do usuário,
     * salvando seus dados e registrando, em seguida, o consentimento associado à requisição.</p>
     *
     * @param usuario usuário que será cadastrado na base de dados.
     * @param request requisição HTTP utilizada para registrar informações relacionadas ao consentimento.
     * @return usuário salvo com os dados persistidos.
     */
    @Transactional
    public Usuario registrarNovoUsuario(Usuario usuario, HttpServletRequest request) {
        Usuario usuarioSalvo = this.salvar(usuario);
        this.incluirConsentimento(usuarioSalvo.getId(), request);
        return usuarioSalvo;
    }

    /**
     * Salva um novo usuário ou atualiza um usuário existente na base de dados.
     *
     * <p>Este método pertence à camada de Service e aplica as regras de persistência de usuário:
     * impede valores nulos, evita cadastro com e-mail duplicado e valida a existência do usuário
     * antes de permitir uma atualização.</p>
     *
     * @param usuario usuário que será cadastrado ou atualizado.
     * @return usuário salvo com os dados persistidos.
     * @throws IllegalArgumentException quando o usuário informado é nulo.
     * @throws DataIntegrityViolationException quando já existe um usuário cadastrado com o mesmo e-mail.
     * @throws EntityNotFoundException quando o usuário informado possui ID, mas não existe na base de dados.
     */
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

    /**
     * Busca um usuário pelo endereço de e-mail informado.
     *
     * <p>Este método pertence à camada de Service e centraliza a consulta de usuário por e-mail,
     * sendo utilizado em validações como verificação de duplicidade no cadastro.</p>
     *
     * @param email endereço de e-mail utilizado para localizar o usuário.
     * @return {@link Optional} contendo o usuário encontrado, ou vazio caso nenhum registro exista para o e-mail informado.
     */
    public Optional<Usuario> buscarPorEmail(String email) {
            return usuarioRepository.findUsuarioByEmail(email);
    }

    /**
     * Busca um usuário pelo seu identificador único.
     *
     * <p>Este método pertence à camada de Service e centraliza a consulta de usuário por ID,
     * lançando exceção quando nenhum registro correspondente é encontrado na base de dados.</p>
     *
     * @param id identificador único do usuário.
     * @return {@link Optional} contendo o usuário encontrado.
     * @throws EntityNotFoundException quando nenhum usuário é encontrado para o identificador informado.
     */
    public Optional<Usuario> buscarPorId(UUID id) {
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findUsuarioById(id);
        if(usuarioEncontrado.isEmpty()){
            throw new EntityNotFoundException("Usuário não encontrado: " + id);
        }

        return usuarioEncontrado;
    }

    /**
     * Registra o consentimento LGPD de um usuário recém-cadastrado.
     *
     * <p>Este método pertence à camada de Service e cria o registro de consentimento
     * associado ao usuário, armazenando a finalidade do uso dos dados e o endereço IP
     * de origem da requisição.</p>
     *
     * @param usuarioId identificador único do usuário que terá o consentimento registrado.
     * @param request requisição HTTP utilizada para obter o endereço IP de origem do usuário.
     */
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


    /**
     * Remove um usuário existente da base de dados.
     *
     * <p>Este método pertence à camada de Service e aplica a regra de exclusão de usuário,
     * verificando previamente se o registro existe antes de executar a remoção.</p>
     *
     * @param id identificador único do usuário que será excluído.
     * @throws EntityNotFoundException quando nenhum usuário é encontrado para o identificador informado.
     */
    public void excluirUsuario(UUID id) {
        var usuarioEncontrado = this.usuarioRepository.findById(id);
        if (usuarioEncontrado.isPresent()) {
            this.usuarioRepository.delete(usuarioEncontrado.get());
        }else {
            throw new EntityNotFoundException("Usuário não encontrado: "+id);
        }
    }

    /**
     * Atualiza os dados sensíveis de autenticação de um usuário.
     *
     * <p>Este método pertence à camada de Service e aplica a regra de atualização de senha,
     * buscando o usuário pelo identificador informado e persistindo o novo hash de senha.</p>
     *
     * @param id identificador único do usuário que terá os dados atualizados.
     * @param senhaHash novo hash de senha que será associado ao usuário.
     * @return usuário atualizado e salvo na base de dados.
     * @throws EntityNotFoundException quando nenhum usuário é encontrado para o identificador informado.
     */
    @Transactional
    public Usuario atualizarDados(UUID id, String senhaHash) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));

        usuario.setSenhaHash(senhaHash);

        return usuarioRepository.save(usuario);
    }

    /**
     * Desativa um usuário existente na base de dados.
     *
     * <p>Este método pertence à camada de Service e aplica a regra de desativação lógica,
     * mantendo o registro do usuário no banco, mas marcando-o como inativo.</p>
     *
     * @param id identificador único do usuário que será desativado.
     * @throws EntityNotFoundException quando nenhum usuário é encontrado para o identificador informado.
     */
    @Transactional
    public void desativarUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

}
