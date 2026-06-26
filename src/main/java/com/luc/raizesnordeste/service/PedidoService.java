package com.luc.raizesnordeste.service;

import com.luc.raizesnordeste.domain.entity.ItemPedido;
import com.luc.raizesnordeste.domain.entity.Pagamento;
import com.luc.raizesnordeste.domain.entity.Pedido;
import com.luc.raizesnordeste.domain.entity.Usuario;
import com.luc.raizesnordeste.domain.enums.CanalPedido;
import com.luc.raizesnordeste.domain.enums.Status;
import com.luc.raizesnordeste.domain.enums.StatusPagamento;
import com.luc.raizesnordeste.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestra o ciclo de vida dos pedidos, validando regras de criação,
 * estoque, integridade de preços, pagamento inicial e transições de status.
 */
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
     * Cria um novo pedido a partir das informações recebidas, aplicando as regras de negócio
     * relacionadas a canal de venda, usuário, produtos, estoque, pagamento e auditoria.
     *
     * <p>Este método pertence à camada de Service e orquestra a criação completa do pedido:
     * valida os dados informados, verifica a existência e disponibilidade dos produtos,
     * debita o estoque, calcula o valor total, cria o pagamento pendente e registra a
     * transição inicial de status para auditoria.</p>
     *
     * @param novoPedido pedido contendo usuário, unidade, canal, itens e forma de pagamento informados.
     * @return pedido salvo com status inicial {@link Status#AGUARDANDO_PAGAMENTO}, valor total calculado
     *         e pagamento pendente associado.
     * @throws IllegalArgumentException quando o canal do pedido é inválido, a lista de itens está vazia,
     *                                  o usuário é inexistente ou inválido, ou algum produto está inativo.
     * @throws EntityNotFoundException quando algum produto ou unidade informado não é encontrado.
     * @throws IllegalStateException quando não há estoque suficiente para algum item do pedido.
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
                throw new IllegalArgumentException("Usuário inexistente ou inválido: " + novoPedido.getUsuario());
            }

            Pedido pedidoParaSalvar = new Pedido();
            BigDecimal valorTotal = BigDecimal.ZERO;

            for (int i = 0; i < novoPedido.getItens().size(); i++) {
                var item = novoPedido.getItens().get(i);

                // Verifica se item existe no Banco
                if (!produtoService.existeProduto(item.getProduto().getId())) {
                    throw new EntityNotFoundException("Item não encontrado: " + item.getProduto().getId());
                }
                // Verifica se o produto está inativo
                if (!produtoService.estaAtivo(item.getProduto().getId())) {
                    throw new IllegalArgumentException("Produto inativo: " + item.getProduto());
                }

                // Verifica se o item possui estoque disponível
                var unidade = novoPedido.getUnidade().getId();
                var itemEstoque = estoqueService.consultarItemEspecifico(unidade, item.getProduto().getId());
                if (itemEstoque.isPresent()) {
                    // o produto existe nessa unidade, necessário validar estoque agora
                    if (item.getQuantidade() <= itemEstoque.get().getQuantidadeAtual()) {
                        // estoque ok, pode criar pedido com o preço do produto no banco (aqui entra a integridade de preço)
                        ItemPedido itemPedido = new ItemPedido();
                        itemPedido.setPrecoUnitario(itemEstoque.get().getProduto().getPrecoBase());
                        itemPedido.setQuantidade(item.getQuantidade());
                        itemPedido.setProduto(itemEstoque.get().getProduto());
                        itemPedido.setPedido(pedidoParaSalvar);
                        pedidoParaSalvar.getItens().add(itemPedido);
                        estoqueService.debitar(item.getProduto().getId(), unidade, item.getQuantidade());
                        valorTotal = valorTotal.add(itemEstoque.get().getProduto().getPrecoBase().multiply(BigDecimal.valueOf(item.getQuantidade())));
                    } else {
                        // retornar erro, estoque insuficiente
                        throw new IllegalStateException("Estoque insuficiente no item [%d]: %s (%s) - Qtd pedida: %d"
                                .formatted(i + 1, itemEstoque.get().getProduto().getNome(), itemEstoque.get().getProduto().getId(), item.getQuantidade()));                    }
                } else {
                    // Retorna erro pois o produto id ou unidade id não existe
                    throw new EntityNotFoundException(String.format("Produto (%s) e/ou Unidade (%s) não encontrados.", item.getProduto().getId(), item.getPedido().getUnidade().getId()));
                }
            }
            pedidoParaSalvar.setCanalPedido(novoPedido.getCanalPedido());
            pedidoParaSalvar.setStatus(Status.AGUARDANDO_PAGAMENTO);
            pedidoParaSalvar.setValorTotal(valorTotal);
            pedidoParaSalvar.setUsuario(novoPedido.getUsuario());
            pedidoParaSalvar.setUnidade(novoPedido.getUnidade());
            pedidoParaSalvar.setPagamento(new Pagamento(novoPedido.getPagamento().getFormaPagamento(), StatusPagamento.PENDENTE, pedidoParaSalvar));
            var pedidoSalvo = pedidoRepository.save(pedidoParaSalvar);
            auditoriaService.registrarTransicao(pedidoParaSalvar, pedidoParaSalvar.getUsuario(), null, Status.AGUARDANDO_PAGAMENTO, "Pedido criado.");
            return pedidoSalvo;
        } else {
            // Retorna que o canal do novoPedido é inválido - 400 Bad Request
            throw new IllegalArgumentException("Canal de novoPedido inválido: " + novoPedido.getCanalPedido());
        }
    }


    /**
     * Atualiza o status de um pedido existente e registra a transição na auditoria.
     *
     * <p>Este método pertence à camada de Service e aplica a regra de mudança de status
     * do pedido, validando o novo status informado, buscando o pedido correspondente e
     * salvando a alteração após o registro da auditoria.</p>
     *
     * @param usuario usuário responsável pela alteração de status.
     * @param pedidoId identificador do pedido que terá o status atualizado.
     * @param novoStatus novo status que será aplicado ao pedido.
     * @param observacao observação opcional relacionada à alteração de status.
     * @return pedido atualizado e salvo com o novo status.
     * @throws EntityNotFoundException quando o novo status informado é inválido ou quando o pedido não é encontrado.
     */
    @Transactional
    public Pedido atualizarStatus(Usuario usuario, UUID pedidoId, Status novoStatus, String observacao){
        if (!Arrays.stream(Status.values()).anyMatch(s -> s.equals(novoStatus))) {
            throw new EntityNotFoundException("Novo status do pedido não encontrado: " + novoStatus);
        }

        Optional<Pedido> pedido = Optional.ofNullable(this.buscarPorId(pedidoId));
        if(pedido.isPresent()){
            auditoriaService.registrarTransicao(pedido.get(), usuario, pedido.get().getStatus(), novoStatus, observacao);
            pedido.get().setStatus(novoStatus);
            return pedidoRepository.save(pedido.get());
        }else{
            throw new EntityNotFoundException("Pedido não encontrado: " + pedidoId);
        }
    }


    /**
     * Busca um pedido pelo seu identificador único.
     *
     * <p>Este método pertence à camada de Service e centraliza a recuperação de um pedido,
     * lançando exceção caso o registro não exista na base de dados.</p>
     *
     * @param id identificador único do pedido.
     * @return pedido encontrado.
     * @throws EntityNotFoundException quando nenhum pedido é encontrado para o identificador informado.
     */
    public Pedido buscarPorId(UUID id){
        return pedidoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + id));
    }

    /**
     * Lista pedidos que permanecem aguardando pagamento há mais de dez minutos.
     *
     * <p>Este método pertence à camada de Service e apoia a regra de controle de pedidos
     * pendentes, permitindo identificar pedidos antigos que ainda não tiveram pagamento confirmado.</p>
     *
     * @return lista de pedidos com status {@link Status#AGUARDANDO_PAGAMENTO} criados há mais de dez minutos.
     */
    public List<Pedido> listarPedidosAguardandoPagamento(){
        return pedidoRepository.findByStatusAndCriadoEmIsBefore(Status.AGUARDANDO_PAGAMENTO, LocalDateTime.now().minusMinutes(10));
    }

    /**
     * Lista os pedidos registrados em um canal de venda específico.
     *
     * <p>Este método pertence à camada de Service e permite consultar pedidos conforme
     * o canal utilizado na criação, como atendimento presencial, aplicativo ou outro canal suportado.</p>
     *
     * @param canal canal de venda utilizado para filtrar os pedidos.
     * @return lista de pedidos associados ao canal informado.
     */
    public List<Pedido> listarPedidosPorCanal(CanalPedido canal){
        return pedidoRepository.findByCanalPedido(canal);
    }

}
