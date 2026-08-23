package com.jonasczaja.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);
}
