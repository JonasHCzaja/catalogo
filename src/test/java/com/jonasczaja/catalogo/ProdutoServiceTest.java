package com.jonasczaja.catalogo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Liga o Mockito. Sem  Spring
public class ProdutoServiceTest {

    @Mock
    ProdutoRepository repository;   // Dublê: não toca no banco

    @InjectMocks
    ProdutoService service;         // Classe real sob teste


    @Test
    void deveRecusarPrecoZero(){

        assertThatThrownBy(() -> service.criar("Café", BigDecimal.ZERO))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("maior que zero");

        verify(repository, never()).save(any());
    }

    @Test
    void deveRecusarNomeDuplicado(){
        when(repository.existsByNome("Café")).thenReturn(true);

        assertThatThrownBy(() -> service.criar("Café", new BigDecimal("24.90")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Já existe");

        verify(repository, never()).save(any());
    }

    @Test
    void deveSalvarProdutoValido(){
        when(repository.existsByNome("Café")).thenReturn(false);
        when(repository.save(any(Produto.class))).thenAnswer(i -> i.getArgument(0));

        Produto criado = service.criar("Café", new BigDecimal("24.90"));

        assertThat(criado.getNome()).isEqualTo("Café");
        verify(repository).save(any(Produto.class));
    }

    @Test
    void deveFalharAoAtualizarInexistente(){
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.atualizar(999L, "Qualquer", new BigDecimal("10.00")))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    void deveRecusarAtualizacaoParaPrecoZero(){
        // ARRANGE
        when(repository.findById(1L)).thenReturn(Optional.of(new Produto()));

        // ACT + ASSERT
        assertThatThrownBy(() -> service.atualizar(1L, "Qualquer", BigDecimal.ZERO))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("maior que zero");

        // ASSERT
        verify(repository, never()).save(any());
    }

    @Test
    void deveAtualizarProdutoValido(){
        // ARRANGE
        Produto existente = new Produto("Qualquer", new BigDecimal("24.90"));
        existente.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Produto.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        Produto atualizado = service.atualizar(1L, "Café", new BigDecimal("34.90"));

        // ASSERT
        assertThat(atualizado.getId()).isEqualTo(1L);
        assertThat(atualizado.getNome()).isEqualTo("Café");
        assertThat(atualizado.getPreco()).isEqualByComparingTo(new BigDecimal("34.90"));
        verify(repository).save(any(Produto.class));
    }

    @Test
    void deveRecusarAtualizacaoParaNomeDuplicado(){
        // ARRANGE
        when(repository.findById(1L)).thenReturn(Optional.of(new Produto("Qualquer", new BigDecimal("24.90"))));
        when(repository.existsByNomeAndIdNot("Café", 1L)).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() ->
                service.atualizar(1L, "Café", new BigDecimal("24.90")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Já existe");

        // ASSERT
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveDeletarQuandoProdutoNaoExiste(){
        when(repository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.deletar(999L))
                .isInstanceOf(ProdutoNaoEncontradoException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    void deveDeletarProdutoExistente(){
        when(repository.existsById(1L)).thenReturn(true);

        service.deletar(1L);

        verify(repository).deleteById(1L);
    }
}
