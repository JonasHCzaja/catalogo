package com.jonasczaja.catalogo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProdutoRepositoryTest {

    @Autowired
    ProdutoRepository repository;

    @Test
    void naoDeveAcusarConflitoComOProprioProduto(){
        // ARRANGE - um único café no banco
        Produto cafe = repository.save(new Produto("Café", new BigDecimal("24.90")));

        // ACT - existe OUTRO produto chamado Café, que não seja este?
        boolean existe = repository.existsByNomeAndIdNot("Café", cafe.getId());

        // ASSERT
        assertThat(existe).isFalse();
    }

    @Test
    void deveAcusarConflitoComOutroProduto(){
        repository.save(new Produto("Café", new BigDecimal("24.90")));
        Produto caneca = repository.save(new Produto("Caneca", new BigDecimal("39.90")));

        boolean existe = repository.existsByNomeAndIdNot("Café", caneca.getId());

        assertThat(existe).isTrue();
    }
}
