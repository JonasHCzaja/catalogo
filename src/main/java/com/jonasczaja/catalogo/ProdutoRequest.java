package com.jonasczaja.catalogo;

import java.math.BigDecimal;

public record ProdutoRequest(String nome, BigDecimal preco) {

    public Produto toEntity(){
        return new Produto(nome, preco);
    }
}
