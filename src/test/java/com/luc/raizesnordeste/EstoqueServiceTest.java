package com.luc.raizesnordeste;

import com.luc.raizesnordeste.domain.entity.Estoque;
import com.luc.raizesnordeste.domain.entity.Produto;
import com.luc.raizesnordeste.domain.entity.Unidade;
import com.luc.raizesnordeste.repository.EstoqueRepository;
import com.luc.raizesnordeste.service.EstoqueService;
import com.luc.raizesnordeste.service.ProdutoService;
import com.luc.raizesnordeste.service.UnidadeService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstoqueService - Regras de débito e crédito de estoque")
class EstoqueServiceTest {

    @Mock
    private EstoqueRepository estoqueRepository;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private EstoqueService estoqueService;

    private UUID produtoId;
    private UUID unidadeId;
    private Estoque estoque;

    @BeforeEach
    void setUp() {
        produtoId = UUID.randomUUID();
        unidadeId = UUID.randomUUID();

        estoque = new Estoque();
        Produto produto = new Produto();
        produto.setId(produtoId);
        Unidade unidade = new Unidade();
        unidade.setId(unidadeId);
        estoque.setProduto(produto);
        estoque.setUnidade(unidade);
        estoque.setQuantidadeAtual(10);
    }

    @Test
    @DisplayName("Deve debitar quantidade do estoque com sucesso quando há saldo suficiente")
    void deveDebitarComSucesso_QuandoSaldoSuficiente() {
        // Arrange
        when(estoqueRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId))
                .thenReturn(Optional.of(estoque));

        // Act
        estoqueService.debitar(produtoId, unidadeId, 4);

        // Assert
        assertEquals(6, estoque.getQuantidadeAtual());
        verify(estoqueRepository, times(1)).save(estoque);
    }

    @Test
    @DisplayName("Deve lançar ArithmeticException ao debitar quantidade maior que o saldo disponível")
    void deveLancarExcecao_QuandoDebitarQuantidadeMaiorQueSaldo() {
        // Arrange
        when(estoqueRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId))
                .thenReturn(Optional.of(estoque));

        // Act & Assert
        ArithmeticException ex = assertThrows(ArithmeticException.class,
                () -> estoqueService.debitar(produtoId, unidadeId, 20));

        assertTrue(ex.getMessage().contains("maior do que o saldo"));
        verify(estoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao debitar quantidade zero ou negativa")
    void deveLancarExcecao_QuandoDebitarQuantidadeInvalida() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> estoqueService.debitar(produtoId, unidadeId, 0));
        verify(estoqueRepository, never()).findByUnidadeIdAndProdutoId(any(), any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao debitar estoque que não existe na unidade")
    void deveLancarExcecao_QuandoDebitarEstoqueInexistente() {
        // Arrange
        when(estoqueRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> estoqueService.debitar(produtoId, unidadeId, 1));
    }

    @Test
    @DisplayName("Deve creditar quantidade ao estoque existente")
    void deveCreditarComSucesso_QuandoEstoqueJaExiste() {
        // Arrange
        when(estoqueRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId))
                .thenReturn(Optional.of(estoque));

        // Act
        estoqueService.creditar(produtoId, unidadeId, 5);

        // Assert
        assertEquals(15, estoque.getQuantidadeAtual());
        verify(estoqueRepository, times(1)).save(estoque);
        verify(produtoService, never()).buscarPorId(any());
    }
    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao creditar produto sem estoque prévio na unidade")
    void deveLancarExcecao_QuandoCreditarSemRegistroPrevio() {
        // Arrange
        when(estoqueRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                estoqueService.creditar(produtoId, unidadeId, 7)
        );

        verify(produtoService).buscarPorId(produtoId);
        verify(estoqueRepository, never()).save(any());
    }
    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao creditar quantidade zero ou negativa")
    void deveLancarExcecao_QuandoCreditarQuantidadeInvalida() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> estoqueService.creditar(produtoId, unidadeId, -1));
        verify(estoqueRepository, never()).findByUnidadeIdAndProdutoId(any(), any());
    }
}