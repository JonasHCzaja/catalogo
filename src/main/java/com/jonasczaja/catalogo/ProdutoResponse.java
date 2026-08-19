package com.jonasczaja.catalogo;

import java.math.BigDecimal;

public record ProdutoResponse(Long id, String nome, BigDecimal preco) {

    public static ProdutoResponse from(Produto p){
        return new ProdutoResponse(p.getId(), p.getNome(), p.getPreco());
    }
}
