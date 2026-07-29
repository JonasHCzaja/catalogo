package com.jonasczaja.catalago;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProdutoController {

    public Produto produto1 = new Produto(1L, "produto1", 20);
    public Produto produto2 = new Produto(2L, "produto2", 30 );

    @GetMapping("/produtos")
    public List<Produto> listar() {
        return List.of(produto1, produto2);
    }

    @GetMapping("/produtos/destaque")
    public Produto destaque() {
        return produto1;
    }
}
