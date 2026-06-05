package com.luc.raizesdeserto.service;

import com.luc.raizesdeserto.domain.entity.ItemPedido;
import com.luc.raizesdeserto.domain.entity.Pagamento;
import com.luc.raizesdeserto.domain.entity.Pedido;
import com.luc.raizesdeserto.domain.entity.Usuario;
import com.luc.raizesdeserto.domain.enums.CanalPedido;
import com.luc.raizesdeserto.domain.enums.Status;
import com.luc.raizesdeserto.domain.enums.StatusPagamento;
import com.luc.raizesdeserto.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;
    private final EstoqueService estoqueService;
    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoService produtoService, EstoqueService estoqueService, UsuarioService usuarioService, AuditoriaService auditoriaService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoService = produtoService;
        this.estoqueService = estoqueService;
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Cria e persiste um novo pedido após validar todas as regras de negócio envolvidas.
     *
     * <p>O processo de criação segue as seguintes etapas:
     * <ol>
     *   <li>Valida se o canal do pedido corresponde a um valor definido em {@link CanalPedido}.</li>
     *   <li>Garante que a lista de itens não esteja vazia.</li>
     *   <li>Valida se o usuário do pedido foi informado ou se existe.</li>
     *   <li>Para cada item, verifica existência, status ativo e disponibilidade de estoque na unidade.</li>
     *   <li>Aplica integridade de preço: o preço unitário utilizado é sempre o {@code precoBase}
     *       cadastrado no banco, ignorando qualquer valor informado na requisição.</li>
     *   <li>Persiste o pedido com status {@code AGUARDANDO_PAGAMENTO} e pagamento {@code PENDENTE}.</li>
     * </ol>
     *
     * @param novoPedido pedido recebido com canal, unidade, usuário, itens e forma de pagamento
     * @return o {@link Pedido} salvo com identificador gerado, valor total calculado e status inicial definido
     * @throws IllegalArgumentException se o canal for inválido, a lista de itens estiver vazia,
     *                                  algum produto estiver inativo ou o estoque for insuficiente
     * @throws EntityNotFoundException  se algum produto, usuário ou a combinação produto/unidade não for encontrada.
     */
    @Transactional
    public Pedido criarPedido(Pedido novoPedido) {
        // Verifica se o canal fornecido no parâmetro existe no enum CanalPedido
        if (Arrays.stream(CanalPedido.values()).anyMatch(c -> c.equals(novoPedido.getCanalPedido()))) {

            // Verifica: se a lista de itens está vazia;
            // e integridade do preço
            if (novoPedido.getItens().isEmpty()) {
                throw new IllegalArgumentException("Lista de itens está vazia.");
            }

            // Verifica se o usuário foi informado no pedido corretamente
            if( novoPedido.getUsuario() == null || usuarioService.buscarPorId(novoPedido.getUsuario().getId()).isEmpty()) {
                throw new IllegalArgumentException("Usuário nulo ou inválido: " + novoPedido.getUsuario());
            }

            Pedido pedidoParaSalvar = new Pedido();
            BigDecimal valorTotal = BigDecimal.ZERO;

            for (int i = 0; i < novoPedido.getItens().size(); i++) {
                var item = novoPedido.getItens().get(i);

                // Verifica se item existe no Banco
                if (!produtoService.existeProduto(item.getProduto().getId())) {
                    throw new EntityNotFoundException("Item não encontrado: " + item);
                }
                // Verifica se o produto está inativo
                if (!produtoService.estaAtivo(item.getProduto().getId())) {
                    throw new IllegalArgumentException("Produto inativo: " + item.getProduto());
                }

                // Verifica se o item possui estoque disponível
                var itemEstoque = estoqueService.consultarItemEspecifico(item.getPedido().getUnidade().getId(), item.getProduto().getId());
                if (itemEstoque.isPresent()) {
                    // o produto existe nessa unidade, necessário validar estoque agora
                    if (item.getQuantidade() <= itemEstoque.get().getQuantidadeAtual()) {
                        // estoque ok, pode criar pedido com o preço do produto no banco (aqui entra a integridade de preço)
                        ItemPedido itemPedido = new ItemPedido();
                        itemPedido.setPrecoUnitario(itemEstoque.get().getProduto().getPrecoBase());
                        itemPedido.setQuantidade(item.getQuantidade());
                        pedidoParaSalvar.getItens().add(itemPedido);
                        estoqueService.debitar(item.getProduto().getId(), item.getPedido().getUnidade().getId(), item.getQuantidade());
                        valorTotal = valorTotal.add(itemEstoque.get().getProduto().getPrecoBase().multiply(BigDecimal.valueOf(item.getQuantidade())));
                    } else {
                        // retornar erro, estoque insuficiente
                        throw new IllegalArgumentException("Estoque insuficiente: " + item.getProduto().getNome() + " - qtd: " + item.getQuantidade());
                    }
                } else {
                    // Retorna erro pois o produto id ou unidade id não existe
                    throw new EntityNotFoundException(String.format("Produto (%s) e/ou Unidade (%s) não encontrados.", item.getProduto().getId(), item.getPedido().getUnidade().getId()));
                }
            }
            pedidoParaSalvar.setCanalPedido(novoPedido.getCanalPedido());
            this.atualizarStatus(pedidoParaSalvar.getUsuario(), pedidoParaSalvar.getId(), Status.AGUARDANDO_PAGAMENTO, "Pedido criado.");
            pedidoParaSalvar.setValorTotal(valorTotal);
            pedidoParaSalvar.setUsuario(novoPedido.getUsuario());
            pedidoParaSalvar.setUnidade(novoPedido.getUnidade());
            pedidoParaSalvar.setPagamento(new Pagamento(novoPedido.getPagamento().getFormaPagamento(), StatusPagamento.PENDENTE));
            auditoriaService.registrarTransicao(pedidoParaSalvar, pedidoParaSalvar.getUsuario(), null, Status.AGUARDANDO_PAGAMENTO, "Pedido criado.");
            return pedidoRepository.save(pedidoParaSalvar);
        } else {
            // Retorna que o canal do novoPedido é inválido - 400 Bad Request
            throw new IllegalArgumentException("Canal de novoPedido inválido: " + novoPedido.getCanalPedido());
        }
    }

    /**
     * Atualiza o status de um pedido, registrando a transição na auditoria.
     *
     * <p>Valida se o novo status é um valor reconhecido do enum {@link Status} e,
     * em seguida, persiste a mudança. Cada transição é registrada via
     * {@code AuditoriaService} antes da atualização, garantindo rastreabilidade
     * das alterações.</p>
     *
     * @param usuario  Usuário responsável pela alteração
     * @param pedidoId   identificador do pedido a ser atualizado
     * @param novoStatus novo status a ser aplicado ao pedido
     * @param observacao comentário opcional justificando a mudança de status
     * @return o {@link Status} atualizado após a persistência
     * @throws EntityNotFoundException se {@code novoStatus} não for um valor válido
     *                                 do enum {@link Status}, ou se o pedido
     *                                 correspondente a {@code pedidoId} não for encontrado
     */
    @Transactional
    public Status atualizarStatus(Usuario usuario, UUID pedidoId, Status novoStatus, String observacao){
        if (!Arrays.stream(Status.values()).anyMatch(s -> s.equals(novoStatus))) {
            throw new EntityNotFoundException("Novo status do pedido não encontrado: " + novoStatus);
        }

        Optional<Pedido> pedido = this.buscarPorId(pedidoId);
        if(pedido.isPresent()){
            auditoriaService.registrarTransicao(pedido.get(), usuario, pedido.get().getStatus(), novoStatus, observacao);
            pedido.get().setStatus(novoStatus);
            return pedidoRepository.save(pedido.get()).getStatus();
        }else{
            throw new EntityNotFoundException("Pedido não encontrado: " + pedidoId);
        }
    }

    public Optional<Pedido> buscarPorId(UUID id){
        return pedidoRepository.findById(id);
    }

    public List<Pedido> listarPedidosAguardandoPagamento(){
        return pedidoRepository.findByStatusAndCriadoEmAfter(Status.AGUARDANDO_PAGAMENTO, LocalDateTime.now().minusMinutes(10));
    }

}
