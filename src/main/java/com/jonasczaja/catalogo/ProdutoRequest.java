package com.jonasczaja.catalogo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProdutoRequest(

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotNull(message = "O preço é obrogatório")
        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal preco) {

    public Produto toEntity(){
        return new Produto(nome, preco);
    }
}
