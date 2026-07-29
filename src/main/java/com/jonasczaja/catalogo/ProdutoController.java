package com.jonasczaja.catalogo;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoRepository repository;

    public ProdutoController(ProdutoRepository repository){
        this.repository = repository;
    }

    @GetMapping
    public List<Produto> listar() {
        return repository.findAll();
    }

    @PostMapping
    public Produto criar(@RequestBody Produto produto){
        return repository.save(produto);
    }

    @GetMapping("/{id}")
    public Optional<Produto> listarPorId(@PathVariable Long id){
        return Optional.ofNullable(repository.findById(id).orElse(null));
    }


}
